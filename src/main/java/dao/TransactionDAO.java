package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Transaction;

/**
 * Owner: Thông / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/TransactionDAO.java.
 */
public class TransactionDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        TransactionDAO.class.getName()
    );
    private final MovieRentalDAO rentalDAO = new MovieRentalDAO();

    /**
     * Tạo giao dịch mới ở trạng thái PENDING.
     * Owner: Thông / Hữu Lộc.
     */
    public boolean create(Transaction tx) {
        String sql =
            "INSERT INTO Transactions (TransactionCode, UserId, TransactionType, PlanId, TargetMovieId, " +
            "OriginalAmount, DiscountAmount, Amount, PaymentMethod, PaymentStatus, BankCode, BankAccountNo, " +
            "BankAccountName, TransferContent, QrCodeUrl, CreatedAt) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, GETDATE())";
        if (tx.getAmount() == null) {
            throw new IllegalArgumentException(
                "Số tiền giao dịch không được để trống"
            );
        }
        String bankCode = requirePaymentValue(tx.getBankCode(), "BankCode");
        String accountNo = requirePaymentValue(
            tx.getBankAccountNo(),
            "BankAccountNo"
        );
        String accountName = requirePaymentValue(
            tx.getBankAccountName(),
            "BankAccountName"
        );
        if (
            tx.getTransactionCode() == null || tx.getTransactionCode().isBlank()
        ) {
            throw new IllegalArgumentException(
                "Mã giao dịch không được để trống"
            );
        }
        String content =
            tx.getTransferContent() == null
                ? "TVHAY " + tx.getTransactionCode()
                : tx.getTransferContent();
        String qrUrl = tx.getQrCodeUrl();
        if (qrUrl == null || qrUrl.isBlank()) {
            qrUrl =
                "https://img.vietqr.io/image/" +
                encodeQuery(bankCode) +
                "-" +
                encodeQuery(accountNo) +
                "-compact2.png?amount=" +
                tx.getAmount().stripTrailingZeros().toPlainString() +
                "&addInfo=" +
                encodeQuery(content) +
                "&accountName=" +
                encodeQuery(accountName);
        }
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setString(1, tx.getTransactionCode());
            ps.setInt(2, tx.getUserId());
            ps.setString(3, tx.getTransactionType());
            if (tx.getPlanId() != null && tx.getPlanId() > 0) {
                ps.setInt(4, tx.getPlanId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (tx.getTargetMovieId() != null && tx.getTargetMovieId() > 0) {
                ps.setInt(5, tx.getTargetMovieId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            BigDecimal amount = tx.getAmount();
            ps.setBigDecimal(
                6,
                tx.getOriginalAmount() == null ? amount : tx.getOriginalAmount()
            );
            ps.setBigDecimal(
                7,
                tx.getDiscountAmount() == null
                    ? BigDecimal.ZERO
                    : tx.getDiscountAmount()
            );
            ps.setBigDecimal(8, amount);
            ps.setString(
                9,
                tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "VIETQR"
            );
            ps.setString(10, bankCode);
            ps.setString(11, accountNo);
            ps.setString(12, accountName);
            ps.setString(13, content);
            ps.setString(14, qrUrl);
            tx.setTransferContent(content);
            tx.setQrCodeUrl(qrUrl);

            int affected = ps.executeUpdate();
            if (affected != 1) {
                throw new SQLException(
                    "Không tạo được giao dịch " + tx.getTransactionCode()
                );
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                        "Không lấy được TransactionId vừa tạo"
                    );
                }
                tx.setTransactionId(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi tạo đơn hàng PENDING: " + tx.getTransactionCode(),
                e
            );
        }
        return false;
    }

    private static String requirePaymentValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống");
        }
        return value.trim();
    }

    private static String encodeQuery(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace(
            "+",
            "%20"
        );
    }

    /**
     * Tìm giao dịch theo mã TransactionCode (vd: TX-12345).
     * Owner: Thông / Hữu Lộc.
     */
    public Transaction findByCode(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            return null;
        }
        String sql =
            "SELECT t.TransactionId, t.TransactionCode, t.UserId, t.TransactionType, " +
            "t.PlanId, t.TargetMovieId, t.OriginalAmount, t.DiscountAmount, t.Amount, " +
            "t.PaymentMethod, t.PaymentStatus, t.BankCode, t.BankAccountNo, t.BankAccountName, " +
            "t.TransferContent, t.QrCodeUrl, t.CreatedAt, t.CompletedAt, u.Username " +
            "FROM Transactions t JOIN Users u ON u.UserId = t.UserId WHERE t.TransactionCode = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, transactionCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi tìm giao dịch theo code=" + transactionCode,
                e
            );
        }
        return null;
    }

    /** Cập nhật giao dịch và kích hoạt quyền lợi trong cùng một transaction.
     * Owner: Thông / Hữu Lộc.
     */
    public boolean markSuccessAndActivate(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            return false;
        }
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Transaction tx = findByCodeForUpdate(
                    conn,
                    transactionCode.trim()
                );
                if (tx == null) {
                    conn.rollback();
                    return false;
                }
                if ("SUCCESS".equalsIgnoreCase(tx.getPaymentStatus())) {
                    conn.commit();
                    return true;
                }
                if (
                    !Transaction.PENDING.equalsIgnoreCase(tx.getPaymentStatus())
                ) {
                    conn.rollback();
                    return false;
                }

                if (tx.isVipPurchase()) {
                    if (tx.getPlanId() == null) {
                        throw new SQLException("Giao dịch VIP không có PlanId");
                    }
                    int durationDays;
                    String planSql =
                        "SELECT DurationDays FROM SubscriptionPlans WHERE PlanId = ?";
                    try (
                        PreparedStatement psPlan = conn.prepareStatement(
                            planSql
                        )
                    ) {
                        psPlan.setInt(1, tx.getPlanId());
                        try (ResultSet rsPlan = psPlan.executeQuery()) {
                            if (!rsPlan.next()) {
                                throw new SQLException(
                                    "Không tìm thấy gói PlanId=" +
                                        tx.getPlanId()
                                );
                            }
                            durationDays = rsPlan.getInt("DurationDays");
                        }
                    }
                    if (durationDays <= 0) {
                        throw new SQLException("Thời hạn gói VIP không hợp lệ");
                    }

                    String vipSql =
                        "UPDATE Users SET IsVip = 1, " +
                        "VipExpireDate = DATEADD(DAY, ?, CASE WHEN VipExpireDate IS NOT NULL AND VipExpireDate > SYSDATETIME() THEN VipExpireDate ELSE SYSDATETIME() END) " +
                        "WHERE UserId = ?";
                    try (
                        PreparedStatement psVip = conn.prepareStatement(vipSql)
                    ) {
                        psVip.setInt(1, durationDays);
                        psVip.setInt(2, tx.getUserId());
                        if (psVip.executeUpdate() != 1) {
                            throw new SQLException(
                                "Không thể cập nhật quyền VIP của user " +
                                    tx.getUserId()
                            );
                        }
                    }
                } else if (tx.isRental()) {
                    if (tx.getTargetMovieId() == null) {
                        throw new SQLException(
                            "Giao dịch thuê phim không có MovieId"
                        );
                    }
                    if (
                        rentalDAO.existsByTransaction(
                            conn,
                            tx.getTransactionId()
                        )
                    ) {
                        throw new SQLException(
                            "Giao dịch đã có vé thuê nhưng vẫn đang PENDING"
                        );
                    }
                    String rentSql =
                        "INSERT INTO MovieRentals (UserId, MovieId, TransactionId, RentedAt, ExpiresAt, Status) " +
                        "VALUES (?, ?, ?, SYSDATETIME(), DATEADD(HOUR, ?, SYSDATETIME()), 'ACTIVE')";
                    try (
                        PreparedStatement psRent = conn.prepareStatement(
                            rentSql
                        )
                    ) {
                        psRent.setInt(1, tx.getUserId());
                        psRent.setInt(2, tx.getTargetMovieId());
                        psRent.setLong(3, tx.getTransactionId());
                        psRent.setInt(4, MovieRentalDAO.RENTAL_HOURS);
                        if (psRent.executeUpdate() != 1) {
                            throw new SQLException(
                                "Không thể tạo vé thuê cho giao dịch " +
                                    tx.getTransactionCode()
                            );
                        }
                    }
                } else {
                    throw new SQLException(
                        "Loại giao dịch không được hỗ trợ: " +
                            tx.getTransactionType()
                    );
                }

                try (
                    PreparedStatement psTx = conn.prepareStatement(
                        "UPDATE Transactions SET PaymentStatus='SUCCESS', CompletedAt=SYSDATETIME() " +
                            "WHERE TransactionId=? AND PaymentStatus='PENDING'"
                    )
                ) {
                    psTx.setLong(1, tx.getTransactionId());
                    if (psTx.executeUpdate() != 1) {
                        throw new SQLException(
                            "Không thể cập nhật trạng thái giao dịch " +
                                tx.getTransactionCode()
                        );
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                LOGGER.log(
                    Level.SEVERE,
                    "Rollback giao dịch cho mã: " + transactionCode,
                    ex
                );
                return false;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException resetError) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Không thể khôi phục autoCommit cho giao dịch " +
                            transactionCode,
                        resetError
                    );
                    throw resetError;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi kết nối khi xử lý giao dịch: " + transactionCode,
                e
            );
            return false;
        }
    }

    /**
     * Tính tổng doanh thu các giao dịch SUCCESS (phục vụ
     * AdminDashboardController).
     * Owner: Thông / Hữu Lộc.
     */
    public double sumRevenue() {
        String sql =
            "SELECT COALESCE(SUM(Amount), 0) FROM Transactions WHERE PaymentStatus = 'SUCCESS'";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tính tổng doanh thu", e);
        }
        return 0.0;
    }

    /**
     * Lấy danh sách giao dịch mới nhất (phục vụ AdminDashboardController).
     * Owner: Thông / Hữu Lộc.
     */
    public List<Transaction> getLatest(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) t.TransactionId, t.TransactionCode, t.UserId, t.TransactionType, " +
            "t.PlanId, t.TargetMovieId, t.OriginalAmount, t.DiscountAmount, t.Amount, " +
            "t.PaymentMethod, t.PaymentStatus, t.BankCode, t.BankAccountNo, t.BankAccountName, " +
            "t.TransferContent, t.QrCodeUrl, t.CreatedAt, t.CompletedAt, u.Username " +
            "FROM Transactions t JOIN Users u ON u.UserId = t.UserId " +
            "ORDER BY t.CreatedAt DESC, t.TransactionId DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách giao dịch mới nhất", e);
        }
        return list;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getLong("TransactionId"));
        t.setTransactionCode(rs.getString("TransactionCode"));
        t.setUserId(rs.getInt("UserId"));
        t.setTransactionType(rs.getString("TransactionType"));
        int planId = rs.getInt("PlanId");
        t.setPlanId(rs.wasNull() ? null : planId);
        int movieId = rs.getInt("TargetMovieId");
        t.setTargetMovieId(rs.wasNull() ? null : movieId);
        t.setOriginalAmount(rs.getBigDecimal("OriginalAmount"));
        t.setDiscountAmount(rs.getBigDecimal("DiscountAmount"));
        t.setAmount(rs.getBigDecimal("Amount"));
        t.setPaymentMethod(rs.getString("PaymentMethod"));
        t.setPaymentStatus(rs.getString("PaymentStatus"));
        t.setBankCode(rs.getString("BankCode"));
        t.setBankAccountNo(rs.getString("BankAccountNo"));
        t.setBankAccountName(rs.getString("BankAccountName"));
        t.setTransferContent(rs.getString("TransferContent"));
        t.setQrCodeUrl(rs.getString("QrCodeUrl"));
        t.setCreatedAt(rs.getTimestamp("CreatedAt"));
        t.setCompletedAt(rs.getTimestamp("CompletedAt"));
        t.setUsername(rs.getString("Username"));
        return t;
    }

    /** Owner: Thông / Hữu Lộc. Purpose: find by code for update. */
    public Transaction findByCodeForUpdate(Connection con, String code)
        throws SQLException {
        String sql =
            "SELECT t.TransactionId, t.TransactionCode, t.UserId, t.TransactionType, t.PlanId, " +
            "t.TargetMovieId, t.OriginalAmount, t.DiscountAmount, t.Amount, t.PaymentMethod, t.PaymentStatus, " +
            "t.BankCode, t.BankAccountNo, t.BankAccountName, t.TransferContent, t.QrCodeUrl, " +
            "t.CreatedAt, t.CompletedAt, u.Username " +
            "FROM Transactions t WITH (UPDLOCK, ROWLOCK) JOIN Users u ON u.UserId = t.UserId " +
            "WHERE t.TransactionCode = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTransaction(rs) : null;
            }
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: find by code for user. */
    public Transaction findByCodeForUser(
        Connection con,
        String code,
        int userId
    ) throws SQLException {
        String sql =
            "SELECT t.TransactionId, t.TransactionCode, t.UserId, t.TransactionType, t.PlanId, " +
            "t.TargetMovieId, t.OriginalAmount, t.DiscountAmount, t.Amount, t.PaymentMethod, t.PaymentStatus, " +
            "t.BankCode, t.BankAccountNo, t.BankAccountName, t.TransferContent, t.QrCodeUrl, " +
            "t.CreatedAt, t.CompletedAt, u.Username " +
            "FROM Transactions t JOIN Users u ON u.UserId = t.UserId " +
            "WHERE t.TransactionCode = ? AND t.UserId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTransaction(rs) : null;
            }
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: insert pending. */
    public long insertPending(
        Connection con,
        Transaction transaction,
        String bankCode
    ) throws SQLException {
        long nextCode;
        try (
            PreparedStatement ps = con.prepareStatement(
                "SELECT ISNULL(MAX(TRY_CONVERT(BIGINT, SUBSTRING(TransactionCode, 4, 50))), 0) + 1 " +
                    "FROM Transactions WITH (UPDLOCK, HOLDLOCK) WHERE TransactionCode LIKE 'TX-%'"
            );
            ResultSet rs = ps.executeQuery()
        ) {
            rs.next();
            nextCode = rs.getLong(1);
        }
        String code = String.format(Locale.ROOT, "TX-%05d", nextCode);
        transaction.setTransactionCode(code);
        transaction.setTransferContent("TVHAY " + code);

        String sql =
            "INSERT INTO Transactions " +
            "(TransactionCode, UserId, TransactionType, PlanId, TargetMovieId, " +
            "OriginalAmount, DiscountAmount, Amount, PaymentMethod, PaymentStatus, " +
            "BankCode, BankAccountNo, BankAccountName, TransferContent, QrCodeUrl) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?)";
        try (
            PreparedStatement ps = con.prepareStatement(sql, new String[] {
                "TransactionId",
            })
        ) {
            ps.setString(1, code);
            ps.setInt(2, transaction.getUserId());
            ps.setString(3, transaction.getTransactionType());
            if (transaction.getPlanId() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, transaction.getPlanId());
            }
            if (transaction.getTargetMovieId() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, transaction.getTargetMovieId());
            }
            ps.setBigDecimal(6, transaction.getOriginalAmount());
            ps.setBigDecimal(7, transaction.getDiscountAmount());
            ps.setBigDecimal(8, transaction.getAmount());
            ps.setString(9, transaction.getPaymentMethod());
            ps.setString(10, bankCode);
            ps.setString(11, transaction.getBankAccountNo());
            ps.setString(12, transaction.getBankAccountName());
            ps.setString(13, transaction.getTransferContent());
            ps.setString(14, transaction.getQrCodeUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                        "Không lấy được TransactionId vừa tạo"
                    );
                }
                long id = keys.getLong(1);
                transaction.setTransactionId(id);
                transaction.setPaymentStatus(Transaction.PENDING);
                return id;
            }
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: mark success. */
    public boolean markSuccess(Connection con, long transactionId)
        throws SQLException {
        String sql =
            "UPDATE Transactions SET PaymentStatus = 'SUCCESS', CompletedAt = SYSDATETIME() " +
            "WHERE TransactionId = ? AND PaymentStatus = 'PENDING'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: mark failed. */
    public boolean markFailed(Connection con, long transactionId)
        throws SQLException {
        String sql =
            "UPDATE Transactions SET PaymentStatus = 'FAILED', CompletedAt = SYSDATETIME() " +
            "WHERE TransactionId = ? AND PaymentStatus = 'PENDING'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: update payment details. */
    public boolean updatePaymentDetails(
        Connection con,
        long transactionId,
        String bankAccountNo,
        String bankAccountName,
        String qrCodeUrl
    ) throws SQLException {
        String sql =
            "UPDATE Transactions SET BankAccountNo = ?, BankAccountName = ?, QrCodeUrl = ? " +
            "WHERE TransactionId = ? AND PaymentStatus = 'PENDING'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bankAccountNo);
            ps.setString(2, bankAccountName);
            ps.setString(3, qrCodeUrl);
            ps.setLong(4, transactionId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: retrieve total revenue. */
    public BigDecimal getTotalRevenue() throws SQLException {
        String q =
            "SELECT COALESCE(SUM(Amount),0) FROM Transactions WHERE PaymentStatus='SUCCESS'";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q);
            ResultSet r = p.executeQuery()
        ) {
            r.next();
            return r.getBigDecimal(1);
        }
    }

    /** Owner: Thông / Hữu Lộc. Purpose: retrieve recent. */
    public List<Transaction> getRecent(int limit) throws SQLException {
        String q =
            "SELECT TOP (?) t.TransactionId, t.TransactionCode, t.UserId, t.TransactionType, " +
            "t.PlanId, t.TargetMovieId, t.OriginalAmount, t.DiscountAmount, t.Amount, " +
            "t.PaymentMethod, t.PaymentStatus, t.BankCode, t.BankAccountNo, t.BankAccountName, " +
            "t.TransferContent, t.QrCodeUrl, t.CreatedAt, t.CompletedAt, u.Username " +
            "FROM Transactions t JOIN Users u ON t.UserId=u.UserId ORDER BY t.CreatedAt DESC";
        List<Transaction> a = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            p.setInt(1, limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    a.add(mapTransaction(r));
                }
            }
        }
        return a;
    }
}
