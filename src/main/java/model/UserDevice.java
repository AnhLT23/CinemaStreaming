package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/UserDevice.java.
 */
public class UserDevice implements Serializable {

    private long deviceId;
    private int userId;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String location;
    private String sessionToken;
    private Timestamp lastActive;
    private Timestamp expiresAt;
    private boolean active;
    private boolean isCurrentDevice;

    public UserDevice() {}

    public UserDevice(
        long deviceId,
        int userId,
        String deviceName,
        String deviceType,
        String ipAddress,
        String location,
        String sessionToken,
        Timestamp lastActive,
        boolean isCurrentDevice
    ) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.location = location;
        this.sessionToken = sessionToken;
        this.lastActive = lastActive;
        this.isCurrentDevice = isCurrentDevice;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Timestamp getLastActive() {
        return lastActive;
    }

    public void setLastActive(Timestamp lastActive) {
        this.lastActive = lastActive;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active || isCurrentDevice;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.isCurrentDevice = active;
    }

    public long getId() {
        return deviceId;
    }

    public String getName() {
        return deviceName;
    }

    public Timestamp getLastSeenAt() {
        return lastActive;
    }

    public boolean isCurrentDevice() {
        return isCurrentDevice;
    }

    public void setCurrentDevice(boolean isCurrentDevice) {
        this.isCurrentDevice = isCurrentDevice;
    }
}
