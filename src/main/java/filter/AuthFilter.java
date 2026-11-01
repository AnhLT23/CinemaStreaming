package filter;

import dao.UserDAO;
import dao.UserDeviceDAO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import ultil.CookieUtil;
import ultil.SecurityUtil;

/**
 * Xác thực phiên, kiểm tra thiết bị đã bị thu hồi; bảo vệ /account, /admin/*,
 * /staff/*.
 */
/**
 * Owner: Lộc.
 * Role: Servlet filter for request encoding, authentication, or session policy.
 * Source: src/main/java/filter/AuthFilter.java.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private final UserDAO users = new UserDAO();
    private final UserDeviceDAO devices = new UserDeviceDAO();

    /** Owner: Lộc. Purpose: filter and authorize the incoming request before dispatch. */
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        String path = req
            .getRequestURI()
            .substring(req.getContextPath().length());
        boolean adminPath = path.equals("/admin") || path.startsWith("/admin/");
        boolean staffPath = path.startsWith("/staff/");
        boolean guarded =
            path.equals("/account") ||
            path.equals("/tai-khoan") ||
            adminPath ||
            staffPath;
        if (!guarded) {
            chain.doFilter(request, response);
            return;
        }
        try {
            HttpSession session = req.getSession(false);
            User signed =
                session == null
                    ? null
                    : (User) session.getAttribute("loggedInUser");
            if (signed != null) {
                User fresh = users.findById(signed.getId());
                Long deviceId = (Long) session.getAttribute("deviceId");
                if (
                    fresh == null ||
                    !fresh.isActive() ||
                    deviceId == null ||
                    !devices.isDeviceActive(fresh.getId(), deviceId)
                ) {
                    session.invalidate();
                    CookieUtil.clear(req, resp, CookieUtil.REMEMBER_COOKIE);
                    signed = null;
                } else {
                    session.setAttribute("loggedInUser", fresh);
                    signed = fresh;
                }
            }
            if (signed == null) {
                String raw = CookieUtil.get(req, CookieUtil.REMEMBER_COOKIE);
                if (raw != null && raw.matches("[A-Za-z0-9_-]{43}")) {
                    String hash = SecurityUtil.sha256(raw);
                    Long uid = devices.findValidUserByToken(hash);
                    if (uid != null) {
                        signed = users.findById(uid);
                        Long restoredDeviceId = devices.findDeviceIdByToken(
                            hash
                        );
                        if (
                            signed != null &&
                            signed.isActive() &&
                            restoredDeviceId != null
                        ) {
                            HttpSession newSession = req.getSession(true);
                            req.changeSessionId();
                            newSession.setMaxInactiveInterval(30 * 60);
                            newSession.setAttribute("loggedInUser", signed);
                            newSession.setAttribute(
                                "deviceId",
                                restoredDeviceId
                            );
                            newSession.setAttribute("theme", signed.getTheme());
                        } else {
                            signed = null;
                        }
                    }
                }
            }
            if (signed == null) {
                if (adminPath || staffPath) {
                    resp.sendError(401);
                } else {
                    resp.sendRedirect(
                        req.getContextPath() + "/login?redirect=%2Faccount"
                    );
                }
                return;
            }
            if (adminPath && !signed.isAdmin()) {
                resp.sendError(403);
                return;
            }
            if (
                staffPath &&
                !(
                    signed.getRoleId() == 2 ||
                    signed.getRoleId() == 8 ||
                    "STAFF".equalsIgnoreCase(signed.getRole()) ||
                    "CUSTOMER_SUPPORT".equalsIgnoreCase(signed.getRole()) ||
                    signed.isAdmin()
                )
            ) {
                resp.sendError(403);
                return;
            }
            chain.doFilter(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Kiểm tra phiên đăng nhập thất bại", ex);
        }
    }

    /** Owner: Lộc. Purpose: initialize servlet or filter state. */
    @Override
    public void init(FilterConfig filterConfig) {}
}
