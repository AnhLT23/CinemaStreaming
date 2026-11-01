package ultil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Owner: Lộc.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/CookieUtil.java.
 */
public final class CookieUtil {

    public static final String REMEMBER_COOKIE = "cinema_remember";
    public static final String THEME_COOKIE = "cinema_theme";

    private CookieUtil() {}

    /** Owner: Lộc. Purpose: perform the get operation. */
    public static String get(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies)
            if (name.equals(c.getName())) return c.getValue();
        return null;
    }

    /** Owner: Lộc. Purpose: perform the set operation. */
    public static void set(
        HttpServletRequest req,
        HttpServletResponse resp,
        String name,
        String value,
        int maxAgeSeconds,
        boolean httpOnly
    ) {
        String path = req.getContextPath();
        if (path.isEmpty()) path = "/";
        String header =
            name +
            "=" +
            value +
            "; Path=" +
            path +
            "; Max-Age=" +
            maxAgeSeconds +
            "; SameSite=Lax" +
            (httpOnly ? "; HttpOnly" : "") +
            (req.isSecure() ? "; Secure" : "");
        resp.addHeader("Set-Cookie", header);
    }

    /** Owner: Lộc. Purpose: perform the clear operation. */
    public static void clear(
        HttpServletRequest req,
        HttpServletResponse resp,
        String name
    ) {
        set(req, resp, name, "", 0, true);
    }
}
