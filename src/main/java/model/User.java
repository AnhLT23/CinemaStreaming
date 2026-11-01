package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Lộc / Hữu Lộc / Thông.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/User.java.
 */
public class User implements Serializable {

    private int userId;
    private String userCode;
    private String username;
    private String passwordHash;
    private String googleId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private boolean isVip;
    private Timestamp vipExpireDate;
    private int roleId;
    private String roleName;
    private String theme;
    private boolean status;
    private Timestamp createdAt;

    public User() {}

    public User(
        int userId,
        String userCode,
        String username,
        String passwordHash,
        String googleId,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        boolean isVip,
        Timestamp vipExpireDate,
        int roleId,
        String roleName,
        String theme,
        boolean status,
        Timestamp createdAt
    ) {
        this.userId = userId;
        this.userCode = userCode;
        this.username = username;
        this.passwordHash = passwordHash;
        this.googleId = googleId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.isVip = isVip;
        this.vipExpireDate = vipExpireDate;
        this.roleId = roleId;
        this.roleName = roleName;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMaskedEmail() {
        if (email == null || email.isBlank()) {
            return "-";
        }
        int separator = email.indexOf('@');
        if (separator <= 0 || separator == email.length() - 1) {
            return "***";
        }
        return email.substring(0, 1) + "***" + email.substring(separator);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // Sửa chuẩn getter cho EL trên JSP (${user.isVip} hoặc ${user.vip})
    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public Timestamp getVipExpireDate() {
        return vipExpireDate;
    }

    public void setVipExpireDate(Timestamp vipExpireDate) {
        this.vipExpireDate = vipExpireDate;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRole() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean isActive() {
        return status;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleName) || roleId == 1;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
