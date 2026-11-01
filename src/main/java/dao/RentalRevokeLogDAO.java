package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.RentalRevokeLog;

/**
 * Owner: Thông.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/RentalRevokeLogDAO.java.
 */
public class RentalRevokeLogDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        RentalRevokeLogDAO.class.getName()
    );

    /** Ghi nhật ký thu hồi vé xem phim vào bảng RentalRevokeLogs.
     * Owner: Thông.
     */
    public boolean insertLog(
        long rentalId,
        int customerUserId,
        int staffUserId,
        String reason
    ) {
        String sql =
            "INSERT INTO RentalRevokeLogs (RentalId, UserId, PerformedBy, RevokeReason, RevokedAt) " +
            "VALUES (?, ?, ?, ?, GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, rentalId);
            ps.setInt(2, customerUserId);
            ps.setInt(3, staffUserId);
            ps.setString(4, reason);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi ghi log thu hồi vé rentalId=" + rentalId,
                e
            );
        }
        return false;
    }

    /** Lấy danh sách lịch sử thu hồi vé gần nhất.
     * Owner: Thông.
     */
    public List<RentalRevokeLog> getRecentLogs(int limit) {
        List<RentalRevokeLog> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) LogId, RentalId, UserId, PerformedBy, RevokeReason, RevokedAt " +
            "FROM RentalRevokeLogs ORDER BY RevokedAt DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 20 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentalRevokeLog log = new RentalRevokeLog();
                    log.setLogId(rs.getLong("LogId"));
                    log.setRentalId(rs.getLong("RentalId"));
                    log.setUserId(rs.getInt("UserId"));
                    log.setPerformedBy(rs.getInt("PerformedBy"));
                    log.setRevokeReason(rs.getString("RevokeReason"));
                    log.setRevokedAt(rs.getTimestamp("RevokedAt"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách RentalRevokeLogs", e);
        }
        return list;
    }

    /** Owner: Thông. Purpose: perform the insert operation. */
    public void insert(Connection con, RentalRevokeLog log)
        throws SQLException {
        String sql =
            "INSERT INTO RentalRevokeLogs (RentalId, UserId, PerformedBy, RevokeReason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, log.getRentalId());
            ps.setInt(2, log.getUserId());
            ps.setInt(3, log.getPerformedBy());
            ps.setString(4, log.getRevokeReason());
            ps.executeUpdate();
        }
    }

    /** Owner: Thông. Purpose: find by user id. */
    public List<RentalRevokeLog> findByUserId(int userId) throws SQLException {
        String sql =
            "SELECT LogId, RentalId, UserId, PerformedBy, RevokeReason, RevokedAt " +
            "FROM RentalRevokeLogs WHERE UserId = ? ORDER BY RevokedAt DESC";
        List<RentalRevokeLog> list = new ArrayList<>();
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentalRevokeLog l = new RentalRevokeLog();
                    l.setLogId(rs.getLong("LogId"));
                    l.setRentalId(rs.getLong("RentalId"));
                    l.setUserId(rs.getInt("UserId"));
                    l.setPerformedBy(rs.getInt("PerformedBy"));
                    l.setRevokeReason(rs.getString("RevokeReason"));
                    Timestamp ts = rs.getTimestamp("RevokedAt");
                    l.setRevokedAt(ts);
                    list.add(l);
                }
            }
        }
        return list;
    }
}
