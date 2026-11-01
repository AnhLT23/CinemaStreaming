package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Owner: Thông / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Transaction.java.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PENDING = "PENDING";
    public static final String TYPE_VIP = "SUBSCRIPTION_VIP";
    public static final String TYPE_RENT = "RENT_MOVIE_48H";

    private long transactionId;
    private String transactionCode;
    private int userId;
    private String transactionType;
    private Integer planId;
    private Integer targetMovieId;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String bankCode;
    private String bankAccountNo;
    private String bankAccountName;
    private String transferContent;
    private String qrCodeUrl;
    private String username;
    private Timestamp createdAt;
    private Timestamp completedAt;

    public Transaction() {}

    public Transaction(
        long transactionId,
        String transactionCode,
        int userId,
        String transactionType,
        Integer planId,
        Integer targetMovieId,
        double amount,
        String paymentMethod,
        String paymentStatus,
        String bankCode,
        Timestamp createdAt,
        Timestamp completedAt
    ) {
        this.transactionId = transactionId;
        this.transactionCode = transactionCode;
        this.userId = userId;
        this.transactionType = transactionType;
        this.planId = planId;
        this.targetMovieId = targetMovieId;
        this.amount = BigDecimal.valueOf(amount);
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.bankCode = bankCode;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getId() {
        return transactionId;
    }

    public void setId(long id) {
        this.transactionId = id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getTargetMovieId() {
        return targetMovieId;
    }

    public void setTargetMovieId(Integer targetMovieId) {
        this.targetMovieId = targetMovieId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return paymentStatus;
    }

    public void setStatus(String status) {
        this.paymentStatus = status;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isVipPurchase() {
        return TYPE_VIP.equalsIgnoreCase(transactionType);
    }

    public boolean isRental() {
        return TYPE_RENT.equalsIgnoreCase(transactionType);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
