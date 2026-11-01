package controller;

import dao.MovieRentalDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import db.DBContext;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import model.Transaction;

/**
 * Owner: Thông.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/PaymentWebhookController.java.
 */
@WebServlet(
    name = "PaymentWebhookController",
    urlPatterns = { "/api/payment/webhook" }
)
public class PaymentWebhookController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(
        PaymentWebhookController.class.getName()
    );
    private static final int MAX_BODY_CHARS = 65_536;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MovieRentalDAO rentalDAO = new MovieRentalDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        req.setCharacterEncoding("UTF-8");
        String body;
        try {
            body = readBody(req);
        } catch (PayloadTooLargeException e) {
            reply(
                resp,
                HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                false,
                "PAYLOAD_TOO_LARGE",
                "Nội dung webhook vượt quá giới hạn"
            );
            return;
        }

        if (!validSignature(body, req.getHeader("X-Signature"))) {
            reply(resp, 401, false, "INVALID_SIGNATURE", "Chữ ký không hợp lệ");
            return;
        }

        String code, gatewayStatus;
        BigDecimal paidAmount;
        try {
            try (
                JsonReader reader = Json.createReader(new StringReader(body))
            ) {
                JsonObject json = reader.readObject();
                code = json.getString("transactionCode").trim();
                gatewayStatus = json
                    .getString("status")
                    .trim()
                    .toUpperCase(java.util.Locale.ROOT);
                paidAmount = json.getJsonNumber("amount").bigDecimalValue();
            }
        } catch (JsonException | ClassCastException | NullPointerException e) {
            reply(
                resp,
                400,
                false,
                "BAD_PAYLOAD",
                "Thiếu hoặc sai trường transactionCode/status/amount"
            );
            return;
        }
        if (
            code.isEmpty() ||
            code.length() > 50 ||
            code.chars().anyMatch(Character::isWhitespace) ||
            (!"SUCCESS".equals(gatewayStatus) &&
                !"FAILED".equals(gatewayStatus)) ||
            paidAmount.signum() < 0
        ) {
            reply(
                resp,
                HttpServletResponse.SC_BAD_REQUEST,
                false,
                "BAD_PAYLOAD",
                "Mã giao dịch, trạng thái hoặc số tiền không hợp lệ"
            );
            return;
        }

        Result result;
        try (Connection con = new DBContext().getConnection()) {
            con.setAutoCommit(false);
            try {
                result = process(con, code, gatewayStatus, paidAmount);

                if (result.commit) {
                    con.commit();
                } else {
                    con.rollback();
                }
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException resetError) {
                    LOG.log(
                        Level.SEVERE,
                        "Không thể khôi phục autoCommit cho webhook, code=" +
                            code,
                        resetError
                    );
                    throw resetError;
                }
            }
        } catch (SQLException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Webhook lỗi, đã rollback. code=" + code, ex);
            reply(
                resp,
                500,
                false,
                "SERVER_ERROR",
                "Lỗi hệ thống, vui lòng gửi lại IPN"
            );
            return;
        }

        reply(resp, 200, result.success, result.code, result.message);
    }

    private Result process(
        Connection con,
        String code,
        String gatewayStatus,
        BigDecimal paidAmount
    ) throws SQLException {
        Transaction tx = transactionDAO.findByCodeForUpdate(con, code);
        if (tx == null) {
            return Result.noCommit(
                false,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn " + code
            );
        }

        if (!Transaction.PENDING.equals(tx.getPaymentStatus())) {
            return Result.noCommit(
                true,
                "DUPLICATE_IPN",
                "Đơn " +
                    code +
                    " đã ở trạng thái " +
                    tx.getPaymentStatus() +
                    ", bỏ qua"
            );
        }

        if (paidAmount.compareTo(tx.getAmount()) != 0) {
            LOG.warning(
                "Sai số tiền đơn " +
                    code +
                    ": nhận " +
                    paidAmount +
                    ", cần " +
                    tx.getAmount()
            );
            return Result.noCommit(
                false,
                "AMOUNT_MISMATCH",
                "Số tiền không khớp, đơn giữ nguyên PENDING"
            );
        }

        if (!"SUCCESS".equals(gatewayStatus)) {
            if (!transactionDAO.markFailed(con, tx.getTransactionId())) {
                throw new SQLException(
                    "Không thể cập nhật trạng thái FAILED cho giao dịch " + code
                );
            }
            return Result.commit(
                true,
                "PAYMENT_FAILED",
                "Đã ghi nhận thanh toán thất bại"
            );
        }

        if (!transactionDAO.markSuccess(con, tx.getTransactionId())) {
            return Result.noCommit(true, "DUPLICATE_IPN", "Đơn đã được xử lý");
        }

        if (tx.isVipPurchase()) {
            if (tx.getPlanId() == null) {
                throw new IllegalStateException(
                    "Đơn VIP không có PlanId: " + code
                );
            }
            int days = userDAO.getPlanDurationDays(con, tx.getPlanId());
            if (days <= 0) {
                throw new IllegalStateException(
                    "Không tìm thấy gói PlanId=" + tx.getPlanId()
                );
            }
            if (!userDAO.extendVip(con, tx.getUserId(), days)) {
                throw new SQLException(
                    "Không thể cộng VIP cho UserId=" + tx.getUserId()
                );
            }
            return Result.commit(true, "OK", "Đã cộng " + days + " ngày VIP");
        } else if (tx.isRental()) {
            if (tx.getTargetMovieId() == null) {
                throw new IllegalStateException(
                    "Đơn thuê không có MovieId: " + code
                );
            }
            if (!rentalDAO.existsByTransaction(con, tx.getTransactionId())) {
                if (
                    !rentalDAO.insertRental(
                        con,
                        tx.getUserId(),
                        tx.getTargetMovieId(),
                        tx.getTransactionId()
                    )
                ) {
                    throw new SQLException(
                        "Không thể tạo vé thuê cho giao dịch " + code
                    );
                }
            }
            return Result.commit(
                true,
                "OK",
                "Đã tạo vé thuê " + MovieRentalDAO.RENTAL_HOURS + " giờ"
            );
        }

        throw new IllegalStateException(
            "TransactionType lạ: " + tx.getTransactionType()
        );
    }

    private static boolean validSignature(String body, String headerSig) {
        String secret = PaymentConfig.getWebhookSecret();
        if (
            headerSig == null ||
            !headerSig.matches("(?i)[0-9a-f]{128}") ||
            secret.isEmpty()
        ) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(
                new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
                )
            );
            byte[] raw = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(
                hex.toString().getBytes(StandardCharsets.UTF_8),
                headerSig.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
            );
        } catch (java.security.GeneralSecurityException e) {
            LOG.log(
                Level.SEVERE,
                "Không thể xác thực chữ ký payment webhook",
                e
            );
            return false;
        }
    }

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            char[] buf = new char[1024];
            int n;
            while ((n = r.read(buf)) != -1) {
                if (sb.length() + n > MAX_BODY_CHARS) {
                    throw new PayloadTooLargeException();
                }
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    private static final class PayloadTooLargeException extends IOException {}

    private static void reply(
        HttpServletResponse resp,
        int http,
        boolean success,
        String code,
        String message
    ) throws IOException {
        JsonObject out = Json.createObjectBuilder()
            .add("success", success)
            .add("code", code)
            .add("message", message)
            .build();
        resp.setStatus(http);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(out.toString());
    }

    private static final class Result {

        final boolean success, commit;
        final String code, message;

        private Result(boolean s, boolean c, String code, String msg) {
            success = s;
            commit = c;
            this.code = code;
            message = msg;
        }

        static Result commit(boolean s, String code, String msg) {
            return new Result(s, true, code, msg);
        }

        static Result noCommit(boolean s, String code, String msg) {
            return new Result(s, false, code, msg);
        }
    }
}
