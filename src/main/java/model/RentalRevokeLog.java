package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Thông.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/RentalRevokeLog.java.
 */
public class RentalRevokeLog implements Serializable {

    private long logId;
    private long rentalId;
    private int userId;
    private int performedBy;
    private String revokeReason;
    private Timestamp revokedAt;

    public RentalRevokeLog() {}

    public RentalRevokeLog(
        long logId,
        long rentalId,
        int userId,
        int performedBy,
        String revokeReason,
        Timestamp revokedAt
    ) {
        this.logId = logId;
        this.rentalId = rentalId;
        this.userId = userId;
        this.performedBy = performedBy;
        this.revokeReason = revokeReason;
        this.revokedAt = revokedAt;
    }

    public RentalRevokeLog(
        long rentalId,
        int userId,
        int performedBy,
        String revokeReason
    ) {
        this(0, rentalId, userId, performedBy, revokeReason, null);
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getRentalId() {
        return rentalId;
    }

    public void setRentalId(long rentalId) {
        this.rentalId = rentalId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(int performedBy) {
        this.performedBy = performedBy;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public void setRevokeReason(String revokeReason) {
        this.revokeReason = revokeReason;
    }

    public Timestamp getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt) {
        this.revokedAt = revokedAt;
    }
}
