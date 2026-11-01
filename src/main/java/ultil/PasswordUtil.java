package ultil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-HMAC-SHA256, muối ngẫu nhiên; không dùng MD5 để lưu mật khẩu. */
/**
 * Owner: Lộc.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/PasswordUtil.java.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;

    private PasswordUtil() {}

    /** Owner: Lộc. Purpose: create a salted password hash. */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return (
            "pbkdf2$" +
            ITERATIONS +
            "$" +
            Base64.getEncoder().encodeToString(salt) +
            "$" +
            Base64.getEncoder().encodeToString(derived)
        );
    }

    /** Owner: Lộc. Purpose: verify a stored password hash. */
    public static boolean verify(String password, String encoded) {
        if (encoded == null) return false;
        if (isLegacyMd5(encoded)) {
            try {
                byte[] raw = MessageDigest.getInstance("MD5").digest(
                    password.getBytes(StandardCharsets.UTF_8)
                );
                String hex = java.util.HexFormat.of().formatHex(raw);
                return MessageDigest.isEqual(
                    hex.getBytes(StandardCharsets.US_ASCII),
                    encoded
                        .toLowerCase(java.util.Locale.ROOT)
                        .getBytes(StandardCharsets.US_ASCII)
                );
            } catch (Exception ex) {
                return false;
            }
        }
        if (!encoded.startsWith("pbkdf2$")) return false;
        try {
            String[] parts = encoded.split("\\$", -1);
            if (parts.length != 4) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length != 16 || expected.length != 32) return false;
            return MessageDigest.isEqual(
                expected,
                derive(password, salt, iterations)
            );
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /** Chỉ nhận diện MD5 đã tồn tại để chuyển đổi sau đăng nhập; không sinh MD5 mới.
     * Owner: Lộc.
     */
    public static boolean isLegacyMd5(String encoded) {
        return encoded != null && encoded.matches("(?i)[a-f0-9]{32}");
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            KEY_BITS
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể băm mật khẩu", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
