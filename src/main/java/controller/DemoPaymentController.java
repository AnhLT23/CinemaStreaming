package controller;

import dao.TransactionDAO;
import db.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import model.Transaction;
import model.User;
import ultil.SecurityUtil;

@WebServlet(name = "DemoPaymentController", urlPatterns = { "/demo-payment" })
public class DemoPaymentController extends HttpServlet {

    private static final String VIEW =
        "/WEB-INF/views/client/demo-payment-confirm.jsp";
    private final TransactionDAO transactions = new TransactionDAO();

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        User user = currentUser(request);
        String code = validCode(request.getParameter("code"));
        if (code == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã giao dịch không hợp lệ."
            );
            return;
        }
        if (user == null) {
            String redirect = "/demo-payment?code=" + code;
            response.sendRedirect(
                request.getContextPath() +
                    "/login?redirect=" +
                    URLEncoder.encode(redirect, StandardCharsets.UTF_8)
            );
            return;
        }

        Transaction transaction = findOwnedDemoTransaction(
            code,
            user.getUserId()
        );
        if (transaction == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy giao dịch demo."
            );
            return;
        }

        SecurityUtil.csrf(request);
        request.setAttribute("transaction", transaction);
        request.setAttribute(
            "watchUrl",
            transaction.isRental() && transaction.getTargetMovieId() != null
                ? request.getContextPath() +
                      "/watch?movieId=" +
                      transaction.getTargetMovieId()
                : request.getContextPath() + "/account"
        );
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(request)) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF token không hợp lệ."
            );
            return;
        }

        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String code = validCode(request.getParameter("code"));
        if (code == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã giao dịch không hợp lệ."
            );
            return;
        }

        Transaction transaction = findOwnedDemoTransaction(
            code,
            user.getUserId()
        );
        if (transaction == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy giao dịch demo."
            );
            return;
        }
        if (
            !Transaction.PENDING.equalsIgnoreCase(
                transaction.getPaymentStatus()
            ) &&
            !"SUCCESS".equalsIgnoreCase(transaction.getPaymentStatus())
        ) {
            response.sendError(
                HttpServletResponse.SC_CONFLICT,
                "Giao dịch không còn ở trạng thái chờ thanh toán."
            );
            return;
        }
        if (!transactions.markSuccessAndActivate(code)) {
            throw new ServletException(
                "Không thể xác nhận thanh toán demo và kích hoạt quyền xem."
            );
        }

        request
            .getSession()
            .setAttribute(
                "successMessage",
                transaction.isRental()
                    ? "Thanh toán demo thành công. Vé xem phim 48 giờ đã được kích hoạt."
                    : "Thanh toán demo thành công. Gói VIP đã được kích hoạt."
            );
        String target =
            transaction.isRental() && transaction.getTargetMovieId() != null
                ? request.getContextPath() +
                  "/watch?movieId=" +
                  transaction.getTargetMovieId()
                : request.getContextPath() + "/account";
        response.sendRedirect(target);
    }

    private Transaction findOwnedDemoTransaction(String code, int userId)
        throws ServletException {
        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Không kết nối được cơ sở dữ liệu.");
            }
            Transaction transaction = transactions.findByCodeForUser(
                connection,
                code,
                userId
            );
            return transaction != null &&
                "DEMO".equalsIgnoreCase(transaction.getPaymentMethod())
                ? transaction
                : null;
        } catch (SQLException ex) {
            throw new ServletException("Không thể đọc giao dịch demo.", ex);
        }
    }

    private static User currentUser(HttpServletRequest request) {
        Object value =
            request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("loggedInUser");
        return value instanceof User ? (User) value : null;
    }

    private static String validCode(String value) {
        return value != null && value.matches("TX-[0-9]{5,}") ? value : null;
    }
}
