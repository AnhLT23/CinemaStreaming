package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/UserToken.java.
 */
public class UserToken implements Serializable {

    private long tokenId;
    private int userId;
    private String series;
    private String tokenHash;
    private Timestamp expiresAt;
    private Timestamp createdAt;

    public UserToken() {}

    public UserToken(
        long tokenId,
        int userId,
        String series,
        String tokenHash,
        Timestamp expiresAt,
        Timestamp createdAt
    ) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.series = series;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
