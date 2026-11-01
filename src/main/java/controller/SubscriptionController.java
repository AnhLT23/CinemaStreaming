package controller;

import dao.MovieRentalDAO;
import dao.SubscriptionDAO;
import dao.TransactionDAO;
import db.DBContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import model.SubscriptionPlan;
import model.Transaction;
import model.User;
import ultil.SecurityUtil;

/**
 * Owner: Thông.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/SubscriptionController.java.
 */
@WebServlet(urlPatterns = { "/subscription", "/vip" })
public class SubscriptionController extends HttpServlet {

    private static final String VIP_VIEW = "/WEB-INF/views/client/vip.jsp";
    private static final String PAYMENT_VIEW =
        "/WEB-INF/views/client/payment-qr.jsp";
    private static final Set<String> PAYMENT_METHODS = Set.of(
        "DEMO",
        "VIETQR",
        "MOMO",
        "VNPAY",
        "CARD"
    );

    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final MovieRentalDAO rentalDAO = new MovieRentalDAO();

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        if ("status".equals(request.getParameter("action"))) {
            writePaymentStatus(request, response);
            return;
        }
        try {
            SecurityUtil.csrf(request);
            request.setAttribute("plans", subscriptionDAO.findActivePlans());
            request.setAttribute(
                "theatricalMovies",
                subscriptionDAO.findTheatricalMovies()
            );
            request.getRequestDispatcher(VIP_VIEW).forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(
                "Không thể tải danh sách gói và phim cho thuê",
                e
            );
        }
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
                "CSRF không hợp lệ"
            );
            return;
        }
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int roleId = user.getRoleId();
        if (roleId == 1 || roleId == 2 || roleId == 8) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Tài khoản nội bộ không được mua gói hoặc thuê phim"
            );
            return;
        }

        String action = request.getParameter("action");
        String method = request.getParameter("paymentMethod");
        if (
            !PAYMENT_METHODS.contains(
                method == null ? "" : method.trim().toUpperCase(Locale.ROOT)
            )
        ) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Phương thức thanh toán không hợp lệ"
            );
            return;
        }

        String bankName;
        String bankAccount;
        String accountName;
        String bankCode;
        boolean demoPayment = "DEMO".equalsIgnoreCase(method.trim());
        if (demoPayment) {
            bankName = "Ngân hàng TVHAY Demo";
            bankAccount = "0000000000";
            accountName = "TAI KHOAN GIA LAP";
            bankCode = "DEMO";
        } else {
            try {
                bankName = PaymentConfig.getBankName();
                bankAccount = PaymentConfig.getBankAccount();
                accountName = PaymentConfig.getAccountName();
                bankCode = PaymentConfig.getBankCode();
            } catch (IllegalStateException ex) {
                getServletContext().log("Thiếu cấu hình thanh toán", ex);
                response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Cổng thanh toán chưa được cấu hình"
                );
                return;
            }
        }

        try (Connection con = new DBContext().getConnection()) {
            con.setAutoCommit(false);
            try {
                Transaction transaction;
                if ("buyPlan".equals(action)) {
                    transaction = createVipOrder(
                        con,
                        request,
                        user,
                        method.trim().toUpperCase(Locale.ROOT)
                    );
                } else if ("rentMovie".equals(action)) {
                    transaction = createRentalOrder(
                        con,
                        request,
                        user,
                        method.trim().toUpperCase(Locale.ROOT)
                    );
                } else {
                    con.rollback();
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Action không hợp lệ"
                    );
                    return;
                }
                transaction.setBankAccountNo(bankAccount);
                transaction.setBankAccountName(accountName);
                transactionDAO.insertPending(con, transaction, bankCode);
                transaction.setQrCodeUrl(
                    demoPayment
                        ? request.getContextPath() +
                              "/api/payment/demo-qr?code=" +
                              transaction.getTransactionCode()
                        : PaymentConfig.createVietQrUrl(
                              transaction.getAmount(),
                              transaction.getTransferContent()
                          )
                );
                if (
                    !transactionDAO.updatePaymentDetails(
                        con,
                        transaction.getTransactionId(),
                        transaction.getBankAccountNo(),
                        transaction.getBankAccountName(),
                        transaction.getQrCodeUrl()
                    )
                ) {
                    throw new SQLException(
                        "Không thể lưu thông tin thanh toán cho giao dịch"
                    );
                }
                con.commit();
                showPaymentPage(
                    request,
                    response,
                    transaction,
                    bankName,
                    bankAccount
                );
            } catch (SQLException | IllegalArgumentException e) {
                con.rollback();
                if (e instanceof IllegalArgumentException) {
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage()
                    );
                    return;
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServletException("Không thể tạo giao dịch thanh toán", e);
        }
    }

    private Transaction createVipOrder(
        Connection con,
        HttpServletRequest request,
        User user,
        String method
    ) throws SQLException {
        int planId = parsePositiveInt(
            request.getParameter("planId"),
            "Gói thành viên không hợp lệ"
        );
        SubscriptionPlan plan = subscriptionDAO.findActivePlan(con, planId);
        if (plan == null) {
            throw new IllegalArgumentException(
                "Gói thành viên không tồn tại hoặc đã ngừng hoạt động"
            );
        }
        BigDecimal amount = BigDecimal.valueOf(plan.getPrice()).setScale(
            2,
            RoundingMode.HALF_UP
        );
        Transaction transaction = new Transaction();
        transaction.setUserId(user.getUserId());
        transaction.setTransactionType(Transaction.TYPE_VIP);
        transaction.setPlanId(planId);
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDiscountAmount(BigDecimal.ZERO.setScale(2));
        transaction.setPaymentMethod(method);
        transaction.setPaymentStatus(Transaction.PENDING);
        return transaction;
    }

    private Transaction createRentalOrder(
        Connection con,
        HttpServletRequest request,
        User user,
        String method
    ) throws SQLException {
        int movieId = parsePositiveInt(
            request.getParameter("movieId"),
            "Phim không hợp lệ"
        );

        if (rentalDAO.hasActiveRental(user.getUserId(), movieId)) {
            throw new IllegalArgumentException(
                "Bạn đang có vé còn hạn của phim này"
            );
        }

        BigDecimal originalAmount = subscriptionDAO.findPublishedRentalPrice(
            con,
            movieId
        );
        if (originalAmount == null) {
            throw new IllegalArgumentException("Phim không khả dụng để thuê");
        }
        originalAmount = originalAmount.setScale(2, RoundingMode.HALF_UP);

        boolean hasActiveVip = subscriptionDAO.isActiveVip(
            con,
            user.getUserId()
        );
        BigDecimal amount = calcRentalPrice(originalAmount, hasActiveVip);
        BigDecimal discount = originalAmount.subtract(amount);

        Transaction transaction = new Transaction();
        transaction.setUserId(user.getUserId());
        transaction.setTransactionType(Transaction.TYPE_RENT);
        transaction.setTargetMovieId(movieId);
        transaction.setOriginalAmount(originalAmount);
        transaction.setDiscountAmount(discount);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(method);
        transaction.setPaymentStatus(Transaction.PENDING);
        return transaction;
    }

    private BigDecimal calcRentalPrice(
        BigDecimal originalAmount,
        boolean hasActiveVip
    ) {
        if (!hasActiveVip) {
            return originalAmount;
        }

        return originalAmount.divide(
            BigDecimal.valueOf(2),
            2,
            RoundingMode.HALF_UP
        );
    }

    private void showPaymentPage(
        HttpServletRequest request,
        HttpServletResponse response,
        Transaction transaction,
        String bankName,
        String bankAccount
    ) throws ServletException, IOException {
        request.setAttribute("transaction", transaction);
        request.setAttribute(
            "qrImageUrl",
            Set.of("VIETQR", "DEMO").contains(transaction.getPaymentMethod())
                ? transaction.getQrCodeUrl()
                : ""
        );
        request.setAttribute("bankName", bankName);
        request.setAttribute("bankAccount", bankAccount);
        request.setAttribute(
            "demoPayment",
            "DEMO".equals(transaction.getPaymentMethod())
        );
        request.setAttribute("countdownSeconds", 900);
        request.setAttribute(
            "successRedirect",
            transaction.isRental() && transaction.getTargetMovieId() != null
                ? request.getContextPath() +
                      "/watch?movieId=" +
                      transaction.getTargetMovieId()
                : request.getContextPath() + "/account"
        );
        request.getRequestDispatcher(PAYMENT_VIEW).forward(request, response);
    }

    private void writePaymentStatus(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Thiếu mã giao dịch"
            );
            return;
        }

        try (Connection con = new DBContext().getConnection()) {
            Transaction transaction = transactionDAO.findByCodeForUser(
                con,
                code.trim(),
                user.getUserId()
            );
            if (transaction == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            JsonObject body = Json.createObjectBuilder()
                .add("status", transaction.getPaymentStatus())
                .build();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(body.toString());
        } catch (SQLException e) {
            throw new IOException(
                "Không thể kiểm tra trạng thái thanh toán",
                e
            );
        }
    }

    private static User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value =
            session == null ? null : session.getAttribute("loggedInUser");
        return value instanceof User ? (User) value : null;
    }

    private static int parsePositiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(message);
    }
}
