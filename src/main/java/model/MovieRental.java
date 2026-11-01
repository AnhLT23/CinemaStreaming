package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Owner: Tuấn Anh / Thông.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/MovieRental.java.
 */
public class MovieRental implements Serializable {

    private static final long serialVersionUID = 1L;

    private long rentalId;
    private int userId;
    private int movieId;
    private long transactionId;
    private Timestamp rentedAt;
    private Timestamp expiresAt;
    private String status;
    private String movieTitle;
    private String userCode;
    private String customerName;
    private String transactionCode;
    private String maskedEmail;

    public MovieRental() {}

    public MovieRental(
        long rentalId,
        int userId,
        int movieId,
        long transactionId,
        Timestamp rentedAt,
        Timestamp expiresAt,
        String status
    ) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.movieId = movieId;
        this.transactionId = transactionId;
        this.rentedAt = rentedAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public long getRentalId() {
        return rentalId;
    }

    public void setRentalId(long rentalId) {
        this.rentalId = rentalId;
    }

    public long getId() {
        return rentalId;
    }

    public void setId(long id) {
        this.rentalId = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getRentedAt() {
        return rentedAt;
    }

    public void setRentedAt(Timestamp rentedAt) {
        this.rentedAt = rentedAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Timestamp getExpireAt() {
        return expiresAt;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRevoked() {
        return status != null && status.equalsIgnoreCase("REVOKED");
    }

    public boolean isExpired() {
        return (
            (status != null && status.equalsIgnoreCase("EXPIRED")) ||
            (expiresAt != null && !expiresAt.toInstant().isAfter(Instant.now()))
        );
    }
}
