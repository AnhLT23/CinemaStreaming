package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.UserDevice;

/**
 * Owner: Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/UserDeviceDAO.java.
 */
public class UserDeviceDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        UserDeviceDAO.class.getName()
    );

    /** Owner: Lộc. Purpose: retrieve devices by user. */
    public List<UserDevice> getDevicesByUser(int userId) {
        List<UserDevice> list = new ArrayList<>();
        String sql =
            "SELECT d.DeviceId, d.UserId, d.DeviceName, d.DeviceType, d.IpAddress, d.Location, " +
            "d.SessionToken, d.LastActive, t.ExpiresAt, " +
            "CASE WHEN d.IsCurrentDevice = 1 AND t.ExpiresAt > GETDATE() " +
            "THEN CAST(1 AS bit) ELSE CAST(0 AS bit) END AS IsCurrentDevice " +
            "FROM UserDevices d LEFT JOIN UserTokens t " +
            "ON t.UserId = d.UserId AND t.TokenHash = d.SessionToken " +
            "WHERE d.UserId = ? ORDER BY IsCurrentDevice DESC, d.LastActive DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserDevice d = new UserDevice();
                    d.setDeviceId(rs.getLong("DeviceId"));
                    d.setUserId(rs.getInt("UserId"));
                    d.setDeviceName(rs.getString("DeviceName"));
                    d.setDeviceType(rs.getString("DeviceType"));
                    d.setIpAddress(rs.getString("IpAddress"));
                    d.setLocation(rs.getString("Location"));
                    d.setSessionToken(rs.getString("SessionToken"));
                    d.setLastActive(rs.getTimestamp("LastActive"));
                    d.setExpiresAt(rs.getTimestamp("ExpiresAt"));
                    d.setCurrentDevice(rs.getBoolean("IsCurrentDevice"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi lấy danh sách thiết bị của userId=" + userId,
                e
            );
        }
        return list;
    }

    /** Owner: Lộc. Purpose: revoke device. */
    public boolean revokeDevice(int userId, long deviceId) {
        try {
            return revokeDevice((long) userId, deviceId);
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi đá phiên thiết bị deviceId=" + deviceId,
                e
            );
            return false;
        }
    }

    /** Owner: Lộc. Purpose: count active devices. */
    public int countActiveDevices() {
        String sql = "SELECT COUNT(*) FROM UserDevices";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm thiết bị online", e);
        }
        return 0;
    }

    /** Owner: Lộc. Purpose: perform the create operation. */
    public long create(
        long userId,
        String tokenHash,
        String deviceName,
        String ipAddress
    ) throws SQLException {
        String tokenSql =
            "INSERT INTO dbo.UserTokens (UserId, Series, TokenHash, ExpiresAt) " +
            "VALUES (?, ?, ?, DATEADD(HOUR, 24, GETDATE()))";
        String deviceSql =
            "INSERT INTO dbo.UserDevices " +
            "(UserId, DeviceName, DeviceType, IpAddress, SessionToken, LastActive, IsCurrentDevice) " +
            "VALUES (?, ?, ?, ?, ?, GETDATE(), 1)";
        String series = UUID.randomUUID().toString();
        String type = classifyDevice(deviceName);
        String name =
            deviceName == null || deviceName.isBlank()
                ? "Trình duyệt"
                : deviceName;
        if (name.length() > 150) {
            name = name.substring(0, 150);
        }
        String ip =
            ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
        if (ip.length() > 50) {
            ip = ip.substring(0, 50);
        }
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (
                PreparedStatement t = c.prepareStatement(tokenSql);
                PreparedStatement d = c.prepareStatement(
                    deviceSql,
                    Statement.RETURN_GENERATED_KEYS
                )
            ) {
                t.setLong(1, userId);
                t.setString(2, series);
                t.setString(3, tokenHash);
                t.executeUpdate();
                d.setLong(1, userId);
                d.setString(2, name);
                d.setString(3, type);
                d.setString(4, ip);
                d.setString(5, tokenHash);
                d.executeUpdate();
                long deviceId;
                try (ResultSet keys = d.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được ID thiết bị");
                    }
                    deviceId = keys.getLong(1);
                }
                c.commit();
                return deviceId;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Owner: Lộc. Purpose: find valid user by token. */
    public Long findValidUserByToken(String tokenHash) throws SQLException {
        String sql =
            "SELECT t.UserId FROM dbo.UserTokens t " +
            "JOIN dbo.UserDevices d ON d.UserId=t.UserId AND d.SessionToken=t.TokenHash " +
            "JOIN dbo.Users u ON u.UserId=t.UserId " +
            "WHERE t.TokenHash=? AND t.ExpiresAt>GETDATE() " +
            "AND d.IsCurrentDevice=1 AND u.Status=1";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, tokenHash);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    /** Owner: Lộc. Purpose: find device id by token. */
    public Long findDeviceIdByToken(String tokenHash) throws SQLException {
        String sql =
            "SELECT d.DeviceId FROM dbo.UserDevices d " +
            "JOIN dbo.UserTokens t ON t.UserId=d.UserId AND t.TokenHash=d.SessionToken " +
            "WHERE d.SessionToken=? AND d.IsCurrentDevice=1 AND t.ExpiresAt>GETDATE()";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, tokenHash);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    /** Owner: Lộc. Purpose: check whether device active. */
    public boolean isDeviceActive(long userId, long deviceId)
        throws SQLException {
        String sql =
            "SELECT 1 FROM dbo.UserDevices d " +
            "JOIN dbo.UserTokens t ON t.UserId=d.UserId AND t.TokenHash=d.SessionToken " +
            "WHERE d.DeviceId=? AND d.UserId=? AND d.IsCurrentDevice=1 AND t.ExpiresAt>GETDATE()";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setLong(1, deviceId);
            p.setLong(2, userId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Owner: Lộc. Purpose: retrieve devices by user. */
    public List<UserDevice> getDevicesByUser(long userId) throws SQLException {
        String sql =
            "SELECT d.DeviceId, d.DeviceName, d.DeviceType, d.LastActive, " +
            "t.ExpiresAt, CASE WHEN d.IsCurrentDevice=1 AND t.ExpiresAt>GETDATE() " +
            "THEN CAST(1 AS bit) ELSE CAST(0 AS bit) END AS Active " +
            "FROM dbo.UserDevices d LEFT JOIN dbo.UserTokens t " +
            "ON t.UserId=d.UserId AND t.TokenHash=d.SessionToken " +
            "WHERE d.UserId=? ORDER BY d.LastActive DESC";
        List<UserDevice> devices = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setLong(1, userId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    UserDevice device = new UserDevice();
                    device.setDeviceId(rs.getLong("DeviceId"));
                    device.setUserId((int) userId);
                    device.setDeviceName(rs.getString("DeviceName"));
                    device.setDeviceType(rs.getString("DeviceType"));
                    device.setLastActive(rs.getTimestamp("LastActive"));
                    device.setExpiresAt(rs.getTimestamp("ExpiresAt"));
                    device.setActive(rs.getBoolean("Active"));
                    devices.add(device);
                }
            }
        }
        return devices;
    }

    /** Owner: Lộc. Purpose: revoke device. */
    public boolean revokeDevice(long userId, long deviceId)
        throws SQLException {
        String select =
            "SELECT SessionToken FROM dbo.UserDevices WHERE DeviceId=? AND UserId=?";
        String update =
            "UPDATE dbo.UserDevices SET IsCurrentDevice=0 WHERE DeviceId=? AND UserId=?";
        String remove =
            "DELETE FROM dbo.UserTokens WHERE UserId=? AND TokenHash=?";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                String hash;
                try (PreparedStatement p = c.prepareStatement(select)) {
                    p.setLong(1, deviceId);
                    p.setLong(2, userId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }
                        hash = rs.getString(1);
                    }
                }
                int count;
                try (PreparedStatement p = c.prepareStatement(update)) {
                    p.setLong(1, deviceId);
                    p.setLong(2, userId);
                    count = p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(remove)) {
                    p.setLong(1, userId);
                    p.setString(2, hash);
                    p.executeUpdate();
                }
                c.commit();
                return count == 1;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Owner: Lộc. Purpose: revoke by token. */
    public boolean revokeByToken(String tokenHash) throws SQLException {
        String update =
            "UPDATE dbo.UserDevices SET IsCurrentDevice=0 WHERE SessionToken=?";
        String remove = "DELETE FROM dbo.UserTokens WHERE TokenHash=?";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (
                PreparedStatement p1 = c.prepareStatement(update);
                PreparedStatement p2 = c.prepareStatement(remove)
            ) {
                p1.setString(1, tokenHash);
                int count = p1.executeUpdate();
                p2.setString(1, tokenHash);
                int removed = p2.executeUpdate();
                c.commit();
                return count > 0 || removed > 0;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private String classifyDevice(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
        if (
            ua.contains("smart-tv") ||
            ua.contains("smarttv") ||
            ua.contains("tizen")
        ) {
            return "SMART_TV";
        }
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "TABLET";
        }
        if (
            ua.contains("mobile") ||
            ua.contains("android") ||
            ua.contains("iphone")
        ) {
            return "MOBILE";
        }
        return "DESKTOP";
    }

    /**
     * Thống kê thiết bị có heartbeat trong khoảng thời gian gần nhất.
     * Owner: Lộc.
     */
    public int countOnlineDevices(int minutes) throws SQLException {
        if (minutes < 1 || minutes > 1440) {
            throw new IllegalArgumentException(
                "Khoảng thời gian phải từ 1 đến 1440 phút."
            );
        }
        String sql =
            "SELECT COUNT(*) FROM UserDevices WHERE LastActive >= DATEADD(MINUTE, ?, GETDATE())";
        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, -minutes);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
