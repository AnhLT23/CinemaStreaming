package controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dao.TransactionDAO;
import db.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.imageio.ImageIO;
import model.Transaction;
import model.User;

@WebServlet(
    name = "DemoPaymentQrServlet",
    urlPatterns = { "/api/payment/demo-qr" }
)
public class DemoPaymentQrServlet extends HttpServlet {

    private static final int QR_SIZE = 280;
    private final TransactionDAO transactions = new TransactionDAO();

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String code = request.getParameter("code");
        if (code == null || !code.matches("TX-[0-9]{5,}")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Không kết nối được cơ sở dữ liệu.");
            }
            Transaction transaction = transactions.findByCodeForUser(
                connection,
                code,
                user.getUserId()
            );
            if (
                transaction == null ||
                !"DEMO".equalsIgnoreCase(transaction.getPaymentMethod()) ||
                !Transaction.PENDING.equalsIgnoreCase(
                    transaction.getPaymentStatus()
                )
            ) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String target = confirmationUrl(request, code);
            BitMatrix matrix = new QRCodeWriter().encode(
                target,
                BarcodeFormat.QR_CODE,
                QR_SIZE,
                QR_SIZE,
                Map.of(EncodeHintType.MARGIN, 1)
            );
            BufferedImage image = new BufferedImage(
                QR_SIZE,
                QR_SIZE,
                BufferedImage.TYPE_INT_RGB
            );
            for (int y = 0; y < QR_SIZE; y++) {
                for (int x = 0; x < QR_SIZE; x++) {
                    image.setRGB(
                        x,
                        y,
                        matrix.get(x, y) ? 0xFF111319 : 0xFFFFFFFF
                    );
                }
            }
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-store");
            ImageIO.write(image, "png", response.getOutputStream());
        } catch (SQLException | WriterException ex) {
            throw new ServletException(
                "Không thể tạo mã QR thanh toán demo.",
                ex
            );
        }
    }

    private static String confirmationUrl(
        HttpServletRequest request,
        String code
    ) throws ServletException {
        String configuredBaseUrl = System.getenv("DEMO_PAYMENT_BASE_URL");
        String origin;
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            try {
                URI uri = URI.create(configuredBaseUrl.trim());
                if (
                    !(
                        "http".equalsIgnoreCase(uri.getScheme()) ||
                        "https".equalsIgnoreCase(uri.getScheme())
                    ) ||
                    uri.getHost() == null ||
                    uri.getRawUserInfo() != null ||
                    uri.getRawQuery() != null ||
                    uri.getRawFragment() != null ||
                    (uri.getRawPath() != null &&
                        !uri.getRawPath().isEmpty() &&
                        !"/".equals(uri.getRawPath()))
                ) {
                    throw new IllegalArgumentException(
                        "Expected an HTTP(S) origin."
                    );
                }
                origin = uri.getScheme() + "://" + uri.getRawAuthority();
            } catch (IllegalArgumentException ex) {
                throw new ServletException(
                    "DEMO_PAYMENT_BASE_URL không hợp lệ.",
                    ex
                );
            }
        } else {
            String scheme = request.getScheme();
            int port = request.getServerPort();
            origin =
                scheme +
                "://" +
                request.getServerName() +
                (("http".equalsIgnoreCase(scheme) && port == 80) ||
                ("https".equalsIgnoreCase(scheme) && port == 443)
                    ? ""
                    : ":" + port);
        }
        return (
            origin +
            request.getContextPath() +
            "/demo-payment?code=" +
            URLEncoder.encode(code, StandardCharsets.UTF_8)
        );
    }

    private static User currentUser(HttpServletRequest request) {
        Object value =
            request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("loggedInUser");
        return value instanceof User ? (User) value : null;
    }
}
