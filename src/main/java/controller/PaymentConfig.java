package controller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Owner: Hữu Lộc / Toàn nhóm.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/PaymentConfig.java.
 */
final class PaymentConfig {

    private PaymentConfig() {}

    static String getBankName() {
        return required("PAYMENT_BANK_NAME");
    }

    static String getBankCode() {
        return required("PAYMENT_BANK_CODE");
    }

    static String getBankAccount() {
        return required("PAYMENT_BANK_ACCOUNT");
    }

    static String getAccountName() {
        return required("PAYMENT_ACCOUNT_NAME");
    }

    static String getWebhookSecret() {
        String secret = System.getenv("PAYMENT_WEBHOOK_SECRET");
        return secret == null ? "" : secret.trim();
    }

    static String createVietQrUrl(BigDecimal amount, String transferContent) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                "Số tiền thanh toán không hợp lệ"
            );
        }
        String bankCode = encode(getBankCode());
        String account = encode(getBankAccount());
        String accountName = encode(getAccountName());
        String info = encode(transferContent == null ? "" : transferContent);
        return (
            "https://img.vietqr.io/image/" +
            bankCode +
            "-" +
            account +
            "-compact2.png?amount=" +
            amount.stripTrailingZeros().toPlainString() +
            "&addInfo=" +
            info +
            "&accountName=" +
            accountName
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Thiếu cấu hình biến môi trường " + name
            );
        }
        return value.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace(
            "+",
            "%20"
        );
    }
}
