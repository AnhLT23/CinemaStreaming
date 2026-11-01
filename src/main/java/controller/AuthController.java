package controller;

import dao.UserDAO;
import dao.UserDeviceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import model.User;
import ultil.CookieUtil;
import ultil.PasswordUtil;
import ultil.SecurityUtil;

/** TV2: /login, /logout, /register. Không phụ thuộc controller của thành viên khác. */
/**
 * Owner: Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/AuthController.java.
 */
@WebServlet(urlPatterns = { "/login", "/logout", "/register" })
public class AuthController extends HttpServlet {

    private final UserDAO users = new UserDAO();
    private final UserDeviceDAO devices = new UserDeviceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            resp.sendError(405, "Vui lòng đăng xuất bằng POST");
            return;
        }
        String redirect = safeLocalRedirect(req.getParameter("redirect"));
        if (
            req.getSession(false) != null &&
            req.getSession(false).getAttribute("loggedInUser") != null
        ) {
            resp.sendRedirect(
                req.getContextPath() +
                    (redirect == null ? "/account" : redirect)
            );
            return;
        }
        SecurityUtil.csrf(req);
        req.setAttribute("redirectPath", redirect == null ? "" : redirect);
        String page = "/register".equals(path)
            ? "/WEB-INF/views/client/register.jsp"
            : "/WEB-INF/views/client/login.jsp";
        req.getRequestDispatcher(page).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(403, "CSRF không hợp lệ");
            return;
        }
        try {
            switch (req.getServletPath()) {
                case "/login":
                    login(req, resp);
                    break;
                case "/register":
                    register(req, resp);
                    break;
                case "/logout":
                    logout(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (SQLException ex) {
            throw new ServletException("Không thể truy cập CSDL", ex);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
        throws SQLException, ServletException, IOException {
        String redirect = safeLocalRedirect(req.getParameter("redirect"));
        req.setAttribute("redirectPath", redirect == null ? "" : redirect);
        String login = param(req, "login", 254);
        String password = req.getParameter("password");
        User user =
            login.isBlank() || password == null
                ? null
                : users.findByLogin(login);
        if (
            user == null ||
            !user.isActive() ||
            !PasswordUtil.verify(password, user.getPasswordHash())
        ) {
            req.setAttribute(
                "error",
                "Tên đăng nhập hoặc mật khẩu không chính xác."
            );
            req.getRequestDispatcher("/WEB-INF/views/client/login.jsp").forward(
                req,
                resp
            );
            return;
        }
        if (PasswordUtil.isLegacyMd5(user.getPasswordHash())) {
            users.updatePassword(
                (long) user.getId(),
                PasswordUtil.hash(password)
            );
        }
        establishSession(req, resp, user);
        resp.sendRedirect(
            req.getContextPath() + (redirect == null ? "/account" : redirect)
        );
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
        throws SQLException, ServletException, IOException {
        String username = param(req, "username", 50);
        String fullName = param(req, "fullName", 100);
        String email = param(req, "email", 100);
        String password = req.getParameter("password");
        String confirmation = req.getParameter("confirmPassword");
        if (
            password == null ||
            !password.equals(confirmation) ||
            !username.matches("[a-zA-Z0-9_]{3,50}") ||
            !email.matches(
                "(?i)^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}$"
            ) ||
            fullName.isBlank() ||
            email.length() > 100 ||
            password.length() < 10 ||
            password.length() > 128
        ) {
            req.setAttribute(
                "error",
                "Kiểm tra họ tên, tên đăng nhập (3–50 ký tự), email (tối đa 100 ký tự) và mật khẩu (10–128 ký tự)."
            );
            req.setAttribute("username", username);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            req.getRequestDispatcher(
                "/WEB-INF/views/client/register.jsp"
            ).forward(req, resp);
            return;
        }
        try {
            long id = users.createCustomer(
                username,
                fullName,
                email,
                PasswordUtil.hash(password)
            );
            User user = users.findById(id);
            establishSession(req, resp, user);
            resp.sendRedirect(req.getContextPath() + "/account");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627) {
                req.setAttribute(
                    "error",
                    "Tên đăng nhập hoặc email đã được sử dụng."
                );
                req.setAttribute("username", username);
                req.setAttribute("fullName", fullName);
                req.setAttribute("email", email);
                req.getRequestDispatcher(
                    "/WEB-INF/views/client/register.jsp"
                ).forward(req, resp);
            } else throw ex;
        }
    }

    private void establishSession(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user
    ) throws SQLException {
        HttpSession session = req.getSession(true);
        req.changeSessionId(); // tránh session fixation khi đăng nhập
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("loggedInUser", user);
        session.setAttribute(
            "theme",
            user.getTheme() == null ? "dark" : user.getTheme()
        );
        String token = SecurityUtil.randomToken();
        long deviceId = devices.create(
            user.getId(),
            SecurityUtil.sha256(token),
            SecurityUtil.userAgent(req),
            req.getRemoteAddr()
        );
        session.setAttribute("deviceId", deviceId);
        CookieUtil.set(
            req,
            resp,
            CookieUtil.REMEMBER_COOKIE,
            token,
            24 * 60 * 60,
            true
        );
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp)
        throws SQLException, IOException {
        String raw = CookieUtil.get(req, CookieUtil.REMEMBER_COOKIE);
        if (raw != null) devices.revokeByToken(SecurityUtil.sha256(raw));
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("loggedInUser");
            Long deviceId = (Long) session.getAttribute("deviceId");
            if (user != null && deviceId != null) devices.revokeDevice(
                user.getId(),
                deviceId
            );
            session.invalidate();
        }
        CookieUtil.clear(req, resp, CookieUtil.REMEMBER_COOKIE);
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    private String param(HttpServletRequest req, String key, int max) {
        String s = req.getParameter(key);
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? "" : s;
    }

    private static String safeLocalRedirect(String value) {
        if (
            value == null ||
            value.isBlank() ||
            value.indexOf('\\') >= 0 ||
            value.indexOf('\r') >= 0 ||
            value.indexOf('\n') >= 0 ||
            value.indexOf('#') >= 0
        ) {
            return null;
        }
        try {
            URI uri = URI.create(value);
            String path = uri.getRawPath();
            if (
                uri.isAbsolute() ||
                uri.getRawAuthority() != null ||
                path == null ||
                !path.startsWith("/") ||
                path.startsWith("//")
            ) {
                return null;
            }
            return value;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
