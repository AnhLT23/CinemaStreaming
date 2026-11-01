package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.MovieRental;
import model.RentalRevokeLog;

/**
 * Owner: Tuấn Anh / Thông.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/MovieRentalDAO.java.
 */
public class MovieRentalDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        MovieRentalDAO.class.getName()
    );

    /** Vé thuê còn hiệu lực (ACTIVE và chưa quá ExpiresAt) của user với phim; null nếu không có.
     * Owner: Tuấn Anh / Thông.
     */
    public MovieRental getActiveRental(int userId, int movieId) {
        String sql =
            "SELECT TOP 1 RentalId, UserId, MovieId, TransactionId, RentedAt, ExpiresAt, Status " +
            "FROM MovieRentals WHERE UserId = ? AND MovieId = ? AND Status = 'ACTIVE' " +
            "AND ExpiresAt > GETDATE() ORDER BY ExpiresAt DESC";
        return queryOne(sql, userId, movieId);
    }

    /** Vé gần nhất (mọi trạng thái) của user với phim, để phân biệt REVOKED / hết hạn.
     * Owner: Tuấn Anh / Thông.
     */
    public MovieRental getLatestRental(int userId, int movieId) {
        String sql =
            "SELECT TOP 1 RentalId, UserId, MovieId, TransactionId, RentedAt, ExpiresAt, Status " +
            "FROM MovieRentals WHERE UserId = ? AND MovieId = ? ORDER BY RentedAt DESC, RentalId DESC";
        return queryOne(sql, userId, movieId);
    }

    /** Danh sách vé còn hạn của user (phục vụ trang hồ sơ).
     * Owner: Tuấn Anh / Thông.
     */
    public List<MovieRental> getActiveRentalsByUser(int userId)
        throws SQLException {
        List<MovieRental> list = new ArrayList<>();
        String sql =
            "SELECT r.RentalId, r.UserId, r.MovieId, r.TransactionId, r.RentedAt, r.ExpiresAt, r.Status, " +
            "m.Title FROM MovieRentals r JOIN Movies m ON m.MovieId = r.MovieId " +
            "WHERE r.UserId = ? AND r.Status = 'ACTIVE' AND r.ExpiresAt > GETDATE() ORDER BY r.ExpiresAt";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovieRental r = map(rs);
                    r.setMovieTitle(rs.getString("Title"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    private MovieRental queryOne(String sql, int userId, int movieId) {
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tra cứu vé thuê", e);
        }
        return null;
    }

    private MovieRental map(ResultSet rs) throws SQLException {
        MovieRental r = new MovieRental();
        r.setRentalId(rs.getLong("RentalId"));
        r.setUserId(rs.getInt("UserId"));
        r.setMovieId(rs.getInt("MovieId"));
        r.setTransactionId(rs.getLong("TransactionId"));
        r.setRentedAt(rs.getTimestamp("RentedAt"));
        r.setExpiresAt(rs.getTimestamp("ExpiresAt"));
        r.setStatus(rs.getString("Status"));
        return r;
    }

    /** Kiểm tra user có vé thuê 48H đang ACTIVE và chưa hết hạn hay không.
     * Owner: Tuấn Anh / Thông.
     */
    public boolean checkUserRentalValid(int userId, int movieId) {
        String sql =
            "SELECT 1 FROM MovieRentals " +
            "WHERE UserId = ? AND MovieId = ? AND Status = 'ACTIVE' AND ExpiresAt > GETDATE()";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi kiểm tra vé thuê 48H cho userId=" +
                    userId +
                    ", movieId=" +
                    movieId,
                ex
            );
        }
        return false;
    }

    /** Alias giữ tương thích với mọi cách gọi trong nhóm.
     * Owner: Tuấn Anh / Thông.
     */
    public boolean hasActiveRental(int userId, int movieId)
        throws SQLException {
        return isRentalActive(userId, movieId);
    }

    /** Kiểm tra vé gần nhất của user với bộ phim có đang bị REVOKED hay không.
     * Owner: Tuấn Anh / Thông.
     */
    public boolean isRentalRevoked(int userId, int movieId) {
        MovieRental latest = getLatestRental(userId, movieId);
        return latest != null && "REVOKED".equalsIgnoreCase(latest.getStatus());
    }

    /** Kiểm tra vé xem của khách vừa bị Staff chuyển sang REVOKED (trả 403 dừng phát video).
     * Owner: Tuấn Anh / Thông.
     */
    public boolean isRentalRevokedForEpisode(int userId, int episodeId) {
        String sql =
            "SELECT 1 FROM MovieRentals mr " +
            "JOIN MovieEpisodes ep ON mr.MovieId = ep.MovieId " +
            "WHERE mr.UserId = ? AND ep.EpisodeId = ? AND mr.Status = 'REVOKED' " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM MovieRentals activeRental " +
            "    WHERE activeRental.UserId = mr.UserId " +
            "      AND activeRental.MovieId = mr.MovieId " +
            "      AND activeRental.Status = 'ACTIVE' " +
            "      AND activeRental.ExpiresAt > GETDATE() " +
            ")";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, episodeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi kiểm tra trạng thái REVOKED cho episodeId=" + episodeId,
                ex
            );
        }
        return false;
    }

    /** Tra cứu danh sách vé thuê của khách theo mã hội viên #TVH-XXXXX hoặc Username (phục vụ StaffRentalController).
     * Owner: Tuấn Anh / Thông.
     */
    public List<MovieRental> findRentalsByCustomerCode(String codeOrUsername) {
        List<MovieRental> list = new ArrayList<>();
        String sql =
            "SELECT r.RentalId, r.UserId, r.MovieId, r.TransactionId, r.RentedAt, r.ExpiresAt, r.Status, m.Title " +
            "FROM MovieRentals r " +
            "JOIN Users u ON r.UserId = u.UserId " +
            "JOIN Movies m ON r.MovieId = m.MovieId " +
            "WHERE u.UserCode = ? OR u.Username = ? OR CAST(r.RentalId AS VARCHAR(20)) = ? " +
            "ORDER BY r.RentedAt DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            String key = codeOrUsername == null ? "" : codeOrUsername.trim();
            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovieRental r = map(rs);
                    r.setMovieTitle(rs.getString("Title"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi tra cứu vé thuê theo mã khách: " + codeOrUsername,
                e
            );
        }
        return list;
    }

    /** Thu hồi vé thuê phim (cập nhật sang REVOKED và ghi dòng lịch sử vào RentalRevokeLogs).
     * Owner: Tuấn Anh / Thông.
     */
    public boolean revokeRental(long rentalId, int staffUserId, String reason) {
        try {
            return revokeWithLog(rentalId, staffUserId, reason);
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi thu hồi vé rentalId=" + rentalId,
                e
            );
            return false;
        }
    }

    /** Owner: Tuấn Anh / Thông. Purpose: retrieve active rentals by user. */
    public List<MovieRental> getActiveRentalsByUser(long userId)
        throws SQLException {
        if (userId <= 0 || userId > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("UserId không hợp lệ");
        }
        return getActiveRentalsByUser((int) userId);
    }

    public static final int RENTAL_HOURS = 48;

    private final RentalRevokeLogDAO logDAO = new RentalRevokeLogDAO();

    /** Owner: Tuấn Anh / Thông. Purpose: check whether by transaction. */
    public boolean existsByTransaction(Connection con, long transactionId)
        throws SQLException {
        try (
            PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM MovieRentals WHERE TransactionId = ?"
            )
        ) {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Owner: Tuấn Anh / Thông. Purpose: check whether rental active. */
    public boolean isRentalActive(int userId, int movieId) throws SQLException {
        String sql =
            "SELECT TOP 1 1 FROM MovieRentals " +
            "WHERE UserId = ? AND MovieId = ? AND Status = 'ACTIVE' " +
            "AND ExpiresAt > SYSDATETIME()";
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Owner: Tuấn Anh / Thông. Purpose: mark expired rentals. */
    public int markExpiredRentals() throws SQLException {
        String sql =
            "UPDATE MovieRentals SET Status = 'EXPIRED' " +
            "WHERE Status = 'ACTIVE' AND ExpiresAt <= SYSDATETIME()";
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            return ps.executeUpdate();
        }
    }

    /** Owner: Tuấn Anh / Thông. Purpose: insert rental. */
    public boolean insertRental(
        Connection con,
        int userId,
        int movieId,
        long transactionId
    ) throws SQLException {
        String sql =
            "INSERT INTO MovieRentals (UserId, MovieId, TransactionId, RentedAt, ExpiresAt, Status) " +
            "VALUES (?, ?, ?, SYSDATETIME(), DATEADD(HOUR, ?, SYSDATETIME()), 'ACTIVE')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setLong(3, transactionId);
            ps.setInt(4, RENTAL_HOURS);
            return ps.executeUpdate() == 1;
        }
    }

    private static final String SELECT_JOIN =
        "SELECT r.RentalId, r.UserId, r.MovieId, r.TransactionId, r.RentedAt, r.ExpiresAt, r.Status, " +
        "m.Title AS MovieTitle, t.TransactionCode, u.UserCode, u.FullName " +
        "FROM MovieRentals r " +
        "JOIN Users u ON u.UserId = r.UserId " +
        "JOIN Movies m ON m.MovieId = r.MovieId " +
        "JOIN Transactions t ON t.TransactionId = r.TransactionId ";

    /** Owner: Tuấn Anh / Thông. Purpose: find by user code. */
    public List<MovieRental> findByUserCode(String userCode)
        throws SQLException {
        String sql =
            SELECT_JOIN + "WHERE u.UserCode = ? ORDER BY r.RentedAt DESC";
        List<MovieRental> list = new ArrayList<>();
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, userCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovieRental rental = mapJoined(rs);
                    list.add(rental);
                }
            }
        }
        return list;
    }

    /** Owner: Tuấn Anh / Thông. Purpose: revoke a rental and record the reason. */
    public boolean revokeWithLog(long rentalId, int staffId, String reason)
        throws SQLException {
        return revokeWithLogInternal(rentalId, null, staffId, reason);
    }

    /** Owner: Tuấn Anh / Thông. Purpose: revoke a rental and record the reason. */
    public boolean revokeWithLog(
        long rentalId,
        String userCode,
        int staffId,
        String reason
    ) throws SQLException {
        return revokeWithLogInternal(rentalId, userCode, staffId, reason);
    }

    private boolean revokeWithLogInternal(
        long rentalId,
        String userCode,
        int staffId,
        String reason
    ) throws SQLException {
        String lockSql =
            userCode == null
                ? "SELECT UserId, Status FROM MovieRentals WITH (UPDLOCK, ROWLOCK) WHERE RentalId = ?"
                : "SELECT r.UserId, r.Status FROM MovieRentals r WITH (UPDLOCK, ROWLOCK) " +
                  "JOIN Users u ON u.UserId = r.UserId WHERE r.RentalId = ? AND u.UserCode = ?";
        try (Connection con = new DBContext().getConnection()) {
            con.setAutoCommit(false);
            try {
                int customerId;
                try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                    ps.setLong(1, rentalId);
                    if (userCode != null) ps.setString(2, userCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        // Chỉ thu hồi vé đang ACTIVE (vé EXPIRED / REVOKED thì từ chối)
                        if (
                            !rs.next() ||
                            !"ACTIVE".equals(rs.getString("Status"))
                        ) {
                            con.rollback();
                            return false;
                        }
                        customerId = rs.getInt("UserId");
                    }
                }

                try (
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE MovieRentals SET Status = 'REVOKED' " +
                            "WHERE RentalId = ? AND Status = 'ACTIVE'"
                    )
                ) {
                    ps.setLong(1, rentalId);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }
                logDAO.insert(
                    con,
                    new RentalRevokeLog(rentalId, customerId, staffId, reason)
                );
                con.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private MovieRental mapJoined(ResultSet rs) throws SQLException {
        MovieRental rental = new MovieRental();
        rental.setRentalId(rs.getLong("RentalId"));
        rental.setUserId(rs.getInt("UserId"));
        rental.setMovieId(rs.getInt("MovieId"));
        rental.setTransactionId(rs.getLong("TransactionId"));
        rental.setRentedAt(rs.getTimestamp("RentedAt"));
        rental.setExpiresAt(rs.getTimestamp("ExpiresAt"));
        rental.setStatus(rs.getString("Status"));
        rental.setMovieTitle(rs.getString("MovieTitle"));
        rental.setTransactionCode(rs.getString("TransactionCode"));
        rental.setUserCode(rs.getString("UserCode"));
        rental.setCustomerName(rs.getString("FullName"));
        return rental;
    }
}
