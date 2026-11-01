package ultil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Owner: Lộc.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/SecurityUtil.java.
 */
public final class SecurityUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtil() {}

    /** Owner: Lộc. Purpose: generate a cryptographically secure random token. */
    public static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Owner: Lộc. Purpose: create or retrieve the session CSRF token. */
    public static String csrf(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            token = randomToken();
            session.setAttribute("csrfToken", token);
        }
        return token;
    }

    /** Owner: Lộc. Purpose: validate the submitted CSRF token. */
    public static boolean validCsrf(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        String expected = (String) session.getAttribute("csrfToken");
        String actual = req.getParameter("csrfToken");
        return (
            expected != null &&
            actual != null &&
            java.security.MessageDigest.isEqual(
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            )
        );
    }

    /** Owner: Lộc. Purpose: compute a SHA-256 token digest. */
    public static String sha256(String token) {
        try {
            byte[] hash = java.security.MessageDigest.getInstance(
                "SHA-256"
            ).digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Owner: Lộc. Purpose: normalize the client user-agent value. */
    public static String userAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) return "Thiết bị không xác định";
        return ua.length() > 160 ? ua.substring(0, 160) : ua;
    }
}
