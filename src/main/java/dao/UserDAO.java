package dao;

import db.DBContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 * Owner: Lộc / Hữu Lộc / Thông.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/UserDAO.java.
 */
public class UserDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        UserDAO.class.getName()
    );

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: compute a legacy MD5 digest. */
    public static String md5(String input) {
        if (input == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không hỗ trợ MD5", e);
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: authenticate a user and establish the session. */
    public User login(String identity, String rawOrHashedPassword) {
        if (identity == null || rawOrHashedPassword == null) {
            return null;
        }
        String trimmed = identity.trim();
        String hash = rawOrHashedPassword.matches("^[a-fA-F0-9]{32}$")
            ? rawOrHashedPassword.toLowerCase()
            : md5(rawOrHashedPassword);

        String sql =
            "SELECT u.UserId, u.UserCode, u.Username, u.PasswordHash, u.GoogleId, u.FullName, " +
            "u.Email, u.Phone, u.AvatarUrl, u.IsVip, u.VipExpireDate, u.RoleId, r.RoleName, " +
            "u.Theme, u.Status, u.CreatedAt " +
            "FROM Users u LEFT JOIN Roles r ON u.RoleId = r.RoleId " +
            "WHERE (u.Username = ? OR u.Email = ? OR u.Phone = ?) AND u.PasswordHash = ? AND u.Status = 1";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, trimmed);
            ps.setString(2, trimmed);
            ps.setString(3, trimmed);
            ps.setString(4, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đăng nhập user: " + trimmed, e);
        }
        return null;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: retrieve by id. */
    public User getById(int userId) {
        String sql =
            "SELECT u.UserId, u.UserCode, u.Username, u.PasswordHash, u.GoogleId, u.FullName, " +
            "u.Email, u.Phone, u.AvatarUrl, u.IsVip, u.VipExpireDate, u.RoleId, r.RoleName, " +
            "u.Theme, u.Status, u.CreatedAt " +
            "FROM Users u LEFT JOIN Roles r ON u.RoleId = r.RoleId WHERE u.UserId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy user theo id=" + userId, e);
        }
        return null;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: validate and create a customer account. */
    public boolean register(
        String username,
        String rawPassword,
        String fullName,
        String email,
        String phone
    ) {
        String userCode = "#TVH-" + (10000 + (int) (Math.random() * 89999));
        String passwordHash = md5(rawPassword);
        String sql =
            "INSERT INTO Users (UserCode, Username, PasswordHash, FullName, Email, Phone, IsVip, RoleId, Theme, Status) " +
            "VALUES (?, ?, ?, ?, ?, ?, 0, 3, 'dark', 1)";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, userCode);
            ps.setString(2, username.trim());
            ps.setString(3, passwordHash);
            ps.setString(
                4,
                fullName != null && !fullName.isBlank()
                    ? fullName.trim()
                    : username.trim()
            );
            ps.setString(5, email.trim());
            ps.setString(
                6,
                phone == null || phone.isBlank() ? null : phone.trim()
            );
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi đăng ký tài khoản mới: " + username,
                e
            );
        }
        return false;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: update password. */
    public boolean updatePassword(int userId, String newRawPassword) {
        String sql = "UPDATE Users SET PasswordHash = ? WHERE UserId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, md5(newRawPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi đổi mật khẩu cho userId=" + userId,
                e
            );
        }
        return false;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: update theme. */
    public boolean updateTheme(int userId, String theme) {
        String cleanTheme = "light".equalsIgnoreCase(theme) ? "light" : "dark";
        String sql = "UPDATE Users SET Theme = ? WHERE UserId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, cleanTheme);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi cập nhật theme cho userId=" + userId,
                e
            );
        }
        return false;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: extend vip. */
    public boolean extendVip(int userId, int durationDays) {
        String sql =
            "UPDATE Users SET IsVip = 1, " +
            "VipExpireDate = DATEADD(DAY, ?, CASE WHEN VipExpireDate IS NOT NULL AND VipExpireDate > GETDATE() THEN VipExpireDate ELSE GETDATE() END) " +
            "WHERE UserId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, durationDays);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gia hạn VIP cho userId=" + userId, e);
        }
        return false;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: retrieve users. */
    public List<User> getUsers(Integer roleId, Boolean status) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT u.UserId, u.UserCode, u.Username, u.PasswordHash, u.GoogleId, u.FullName, " +
                "u.Email, u.Phone, u.AvatarUrl, u.IsVip, u.VipExpireDate, u.RoleId, r.RoleName, " +
                "u.Theme, u.Status, u.CreatedAt " +
                "FROM Users u LEFT JOIN Roles r ON u.RoleId = r.RoleId WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (roleId != null && roleId > 0) {
            sql.append(" AND u.RoleId = ? ");
            params.add(roleId);
        }
        if (status != null) {
            sql.append(" AND u.Status = ? ");
            params.add(status);
        }
        sql.append(" ORDER BY u.UserId ASC");

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách hội viên", e);
        }
        return list;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: toggle status. */
    public boolean toggleStatus(int userId) {
        String sql =
            "UPDATE Users SET Status = CASE WHEN Status = 1 THEN 0 ELSE 1 END WHERE UserId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khóa/mở khóa userId=" + userId, e);
        }
        return false;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: count vip users. */
    public int countVipUsers() {
        String sql =
            "SELECT COUNT(*) FROM Users WHERE IsVip = 1 AND Status = 1";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số hội viên VIP", e);
        }
        return 0;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("UserId"));
        u.setUserCode(rs.getString("UserCode"));
        u.setUsername(rs.getString("Username"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setGoogleId(rs.getString("GoogleId"));
        u.setFullName(rs.getString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setPhone(rs.getString("Phone"));
        u.setAvatarUrl(rs.getString("AvatarUrl"));
        u.setVip(rs.getBoolean("IsVip"));
        Timestamp vipExp = rs.getTimestamp("VipExpireDate");
        u.setVipExpireDate(vipExp);
        u.setRoleId(rs.getInt("RoleId"));
        u.setRoleName(rs.getString("RoleName"));
        u.setTheme(rs.getString("Theme"));
        u.setStatus(rs.getBoolean("Status"));
        u.setCreatedAt(rs.getTimestamp("CreatedAt"));
        return u;
    }

    private static final String USER_SELECT =
        "SELECT u.*, r.RoleName " +
        "FROM dbo.Users u JOIN dbo.Roles r ON r.RoleId = u.RoleId ";

    private User read(ResultSet rs) throws SQLException {
        return mapUser(rs);
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: find by login. */
    public User findByLogin(String login) throws SQLException {
        String sql = USER_SELECT + "WHERE u.Username = ? OR u.Email = ?";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, login);
            p.setString(2, login);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: find by id. */
    public User findById(long id) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                USER_SELECT + "WHERE u.UserId = ?"
            )
        ) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: create customer. */
    public long createCustomer(
        String username,
        String fullName,
        String email,
        String passwordHash
    ) throws SQLException {
        String sql =
            "INSERT INTO dbo.Users (UserCode, Username, FullName, Email, PasswordHash, RoleId, Theme, Status) " +
            "VALUES (?, ?, ?, ?, ?, (SELECT RoleId FROM dbo.Roles WHERE RoleName='CUSTOMER'), 'dark', 1)";
        String code =
            "#TVH-" +
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            p.setString(1, code);
            p.setString(2, username);
            p.setString(3, fullName);
            p.setString(4, email);
            p.setString(5, passwordHash);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Không lấy được mã tài khoản mới");
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: update password. */
    public boolean updatePassword(long userId, String passwordHash)
        throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "UPDATE dbo.Users SET PasswordHash=? WHERE UserId=? AND Status=1"
            )
        ) {
            p.setString(1, passwordHash);
            p.setLong(2, userId);
            return p.executeUpdate() == 1;
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: update theme. */
    public boolean updateTheme(long userId, String theme) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "UPDATE dbo.Users SET Theme=? WHERE UserId=? AND Status=1"
            )
        ) {
            p.setString(1, theme);
            p.setLong(2, userId);
            return p.executeUpdate() == 1;
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: retrieve vip expire date. */
    public Timestamp getVipExpireDate(long userId) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "SELECT VipExpireDate FROM dbo.Users WHERE UserId=?"
            )
        ) {
            p.setLong(1, userId);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? rs.getTimestamp(1) : null;
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: retrieve users. */
    public List<User> getUsers(
        String keyword,
        String status,
        int page,
        int pageSize
    ) throws SQLException {
        String sql =
            USER_SELECT +
            "WHERE (?='' OR u.Username LIKE ? OR u.Email LIKE ? OR u.FullName LIKE ?) " +
            "AND (?='ALL' OR (?='ACTIVE' AND u.Status=1) OR (?='LOCKED' AND u.Status=0)) " +
            "ORDER BY u.UserId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<User> list = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            String q = keyword == null ? "" : keyword.trim();
            String like =
                "%" +
                q.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]") +
                "%";
            p.setString(1, q);
            p.setString(2, like);
            p.setString(3, like);
            p.setString(4, like);
            p.setString(5, status);
            p.setString(6, status);
            p.setString(7, status);
            p.setInt(8, (page - 1) * pageSize);
            p.setInt(9, pageSize);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(read(rs));
                }
            }
        }
        return list;
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: toggle status. */
    public boolean toggleStatus(long id) throws SQLException {
        String sql =
            "UPDATE dbo.Users SET Status=CASE WHEN Status=1 THEN 0 ELSE 1 END " +
            "WHERE UserId=? AND RoleId=(SELECT RoleId FROM dbo.Roles WHERE RoleName='CUSTOMER')";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setLong(1, id);
            return p.executeUpdate() == 1;
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: extend vip. */
    public boolean extendVip(Connection con, int userId, int durationDays)
        throws SQLException {
        String sql =
            "UPDATE Users SET IsVip = 1, " +
            "VipExpireDate = DATEADD(DAY, ?, " +
            "  CASE WHEN VipExpireDate IS NOT NULL AND VipExpireDate > SYSDATETIME() " +
            "       THEN VipExpireDate ELSE SYSDATETIME() END) " +
            "WHERE UserId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, durationDays);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: retrieve plan duration days. */
    public int getPlanDurationDays(Connection con, int planId)
        throws SQLException {
        try (
            PreparedStatement ps = con.prepareStatement(
                "SELECT DurationDays FROM SubscriptionPlans WHERE PlanId = ?"
            )
        ) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: find by user code. */
    public User findByUserCode(String userCode) throws SQLException {
        String sql =
            "SELECT UserId, UserCode, Username, FullName, Email, Phone, IsVip, " +
            "VipExpireDate, RoleId, Status FROM Users WHERE UserCode = ?";
        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, userCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User u = new User();
                u.setUserId(rs.getInt("UserId"));
                u.setUserCode(rs.getString("UserCode"));
                u.setUsername(rs.getString("Username"));
                u.setFullName(rs.getString("FullName"));
                u.setEmail(rs.getString("Email"));
                u.setPhone(rs.getString("Phone"));
                u.setVip(rs.getBoolean("IsVip"));
                Timestamp ts = rs.getTimestamp("VipExpireDate");
                u.setVipExpireDate(ts);
                u.setRoleId(rs.getInt("RoleId"));
                u.setStatus(rs.getBoolean("Status"));
                return u;
            }
        }
    }

    /** Owner: Lộc / Hữu Lộc / Thông. Purpose: count vip active. */
    public int countVipActive() throws SQLException {
        String q =
            "SELECT COUNT(*) FROM Users WHERE IsVip=1 AND Status=1 AND (VipExpireDate IS NULL OR VipExpireDate>GETDATE())";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q);
            ResultSet r = p.executeQuery()
        ) {
            r.next();
            return r.getInt(1);
        }
    }
}
