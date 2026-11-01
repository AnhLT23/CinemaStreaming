package controller;

import dao.MovieRentalDAO;
import dao.UserDAO;
import dao.UserDeviceDAO;
import dao.WatchHistoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import ultil.CookieUtil;
import ultil.PasswordUtil;
import ultil.SecurityUtil;

/**
 * Owner: Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/AccountController.java.
 */
@WebServlet(
    name = "AccountController",
    urlPatterns = { "/account", "/tai-khoan" }
)
public class AccountController extends HttpServlet {

    private final UserDAO users = new UserDAO();
    private final UserDeviceDAO devices = new UserDeviceDAO();
    private final MovieRentalDAO rentals = new MovieRentalDAO();
    private final WatchHistoryDAO watchHistory = new WatchHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("loggedInUser"); // AuthFilter đã bảo vệ URL này
        try {
            User profile = users.findById(me.getId());
            req.setAttribute("profile", profile);
            req.setAttribute("roleCategory", roleCategory(profile));
            req.setAttribute(
                "vipExpireDate",
                users.getVipExpireDate(me.getId())
            );
            req.setAttribute(
                "activeRentals",
                rentals.getActiveRentalsByUser(me.getId())
            );
            req.setAttribute("myDevices", devices.getDevicesByUser(me.getId()));
            req.setAttribute(
                "watchHistory",
                watchHistory.getRecentByUserId(me.getUserId(), 50)
            );
            req.setAttribute(
                "currentDeviceId",
                req.getSession().getAttribute("deviceId")
            );
            SecurityUtil.csrf(req);
            req.getRequestDispatcher(
                "/WEB-INF/views/client/account.jsp"
            ).forward(req, resp);
        } catch (SQLException ex) {
            throw new ServletException("Lỗi đọc thông tin tài khoản", ex);
        }
    }

    private static String roleCategory(User user) {
        if (user.isAdmin()) return "admin";
        if (
            user.getRoleId() == 2 ||
            user.getRoleId() == 8 ||
            "STAFF".equalsIgnoreCase(user.getRoleName()) ||
            "CUSTOMER_SUPPORT".equalsIgnoreCase(user.getRoleName())
        ) {
            return "staff";
        }
        if (
            user.getRoleId() == 9 ||
            "GUEST".equalsIgnoreCase(user.getRoleName())
        ) {
            return "guest";
        }
        return "customer";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(403);
            return;
        }
        User me = (User) req.getSession().getAttribute("loggedInUser");
        String action = req.getParameter("action");
        try {
            if ("revoke".equals(action)) {
                long id;
                try {
                    id = Long.parseLong(req.getParameter("deviceId"));
                } catch (Exception ex) {
                    resp.sendError(400);
                    return;
                }
                if (id <= 0) {
                    resp.sendError(400);
                    return;
                }
                if (!devices.revokeDevice(me.getId(), id)) {
                    resp.sendError(404);
                    return;
                }
                if (
                    Long.valueOf(id).equals(
                        req.getSession().getAttribute("deviceId")
                    )
                ) {
                    req.getSession().invalidate();
                    CookieUtil.clear(req, resp, CookieUtil.REMEMBER_COOKIE);
                    resp.sendRedirect(req.getContextPath() + "/login");
                    return;
                }
                resp.sendRedirect(
                    req.getContextPath() + "/account?message=revoked"
                );
                return;
            }
            if ("password".equals(action)) {
                String old = req.getParameter("oldPassword");
                String updated = req.getParameter("newPassword");
                User persisted = users.findById(me.getId());
                if (
                    old == null ||
                    updated == null ||
                    !PasswordUtil.verify(old, persisted.getPasswordHash()) ||
                    updated.length() < 10 ||
                    updated.length() > 128
                ) {
                    resp.sendRedirect(
                        req.getContextPath() +
                            "/account?message=password_invalid"
                    );
                    return;
                }
                if (
                    !users.updatePassword(
                        (long) me.getId(),
                        PasswordUtil.hash(updated)
                    )
                ) {
                    throw new SQLException("Không thể cập nhật mật khẩu");
                }
                resp.sendRedirect(
                    req.getContextPath() + "/account?message=password_updated"
                );
                return;
            }
            resp.sendError(400, "Thao tác không hợp lệ");
        } catch (SQLException ex) {
            throw new ServletException("Lỗi cập nhật tài khoản", ex);
        }
    }
}
