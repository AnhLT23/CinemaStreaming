package controller;

import dao.MovieRentalDAO;
import dao.RentalRevokeLogDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.MovieRental;
import model.User;

/**
 * Owner: Thông.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/StaffRentalController.java.
 */
@WebServlet(name = "StaffRentalController", urlPatterns = { "/staff/rentals" })
public class StaffRentalController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(
        StaffRentalController.class.getName()
    );
    private static final String VIEW =
        "/WEB-INF/views/staff/rental-support.jsp";

    private static boolean isStaffRole(int roleId) {
        return roleId == 1 || roleId == 2 || roleId == 8;
    }

    private static final int MAX_REASON = 255;

    private final UserDAO userDAO = new UserDAO();
    private final MovieRentalDAO rentalDAO = new MovieRentalDAO();
    private final RentalRevokeLogDAO logDAO = new RentalRevokeLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        HttpSession existingSession = req.getSession(false);
        Object userValue =
            existingSession == null
                ? null
                : existingSession.getAttribute("loggedInUser");
        if (!(userValue instanceof User)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User staff = (User) userValue;
        if (!isStaffRole(staff.getRoleId())) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền truy cập"
            );
            return;
        }
        HttpSession session = req.getSession();

        try {
            rentalDAO.markExpiredRentals();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không thể cập nhật vé hết hạn", e);
        }

        moveFlash(session, req, "flashOk");
        moveFlash(session, req, "flashError");

        req.setAttribute("csrfToken", csrfToken(session));
        String code = normalizeCode(req.getParameter("code"));
        req.setAttribute("code", code == null ? "" : code);

        if (code != null) {
            try {
                User customer = userDAO.findByUserCode(code);
                if (customer == null) {
                    req.setAttribute(
                        "searchError",
                        "Không tìm thấy khách hàng có mã " + code
                    );
                } else {
                    List<MovieRental> rentals = rentalDAO.findByUserCode(code);
                    req.setAttribute("customerCode", customer.getUserCode());
                    req.setAttribute("customerName", customer.getFullName());
                    req.setAttribute(
                        "maskedEmail",
                        maskEmail(customer.getEmail())
                    );
                    req.setAttribute(
                        "maskedPhone",
                        maskPhone(customer.getPhone())
                    );
                    req.setAttribute("rentals", rentals);
                    req.setAttribute(
                        "revokeLogs",
                        logDAO.findByUserId(customer.getUserId())
                    );
                }
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Lỗi tra cứu vé, code=" + code, e);
                req.setAttribute(
                    "searchError",
                    "Lỗi hệ thống khi tra cứu, vui lòng thử lại."
                );
            }
        }
        req.setAttribute(
            "rentals",
            req.getAttribute("rentals") == null
                ? Collections.emptyList()
                : req.getAttribute("rentals")
        );
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        User staff = requireStaff(req, resp);
        if (staff == null) return;
        HttpSession session = req.getSession();
        req.setCharacterEncoding("UTF-8");
        if (!"revoke".equals(req.getParameter("action"))) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Action không được hỗ trợ"
            );
            return;
        }

        String expected = (String) session.getAttribute("csrfToken");
        if (
            expected == null || !expected.equals(req.getParameter("csrfToken"))
        ) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Phiên làm việc không hợp lệ"
            );
            return;
        }

        String code = normalizeCode(req.getParameter("code"));
        String reason =
            req.getParameter("reason") == null
                ? ""
                : req.getParameter("reason").trim();
        long rentalId;
        try {
            rentalId = Long.parseLong(req.getParameter("rentalId"));
            if (rentalId <= 0) throw new NumberFormatException(
                "rentalId must be positive"
            );
        } catch (NumberFormatException e) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "rentalId không hợp lệ"
            );
            return;
        }
        if (code == null) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã khách hàng không hợp lệ"
            );
            return;
        }

        if (reason.isEmpty() || reason.length() > MAX_REASON) {
            flashAndBack(
                session,
                resp,
                req,
                code,
                "flashError",
                "Vui lòng nhập lý do thu hồi, tối đa " + MAX_REASON + " ký tự."
            );
            return;
        }

        try {
            boolean ok = rentalDAO.revokeWithLog(
                rentalId,
                code,
                staff.getUserId(),
                reason
            );
            if (ok) flashAndBack(
                session,
                resp,
                req,
                code,
                "flashOk",
                "Đã thu hồi vé #" + rentalId + " và ghi nhật ký."
            );
            else flashAndBack(
                session,
                resp,
                req,
                code,
                "flashError",
                "Vé không tồn tại hoặc không còn ở trạng thái đang hoạt động."
            );
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi thu hồi vé " + rentalId, e);
            flashAndBack(
                session,
                resp,
                req,
                code,
                "flashError",
                "Lỗi hệ thống, vé chưa bị thu hồi."
            );
        }
    }

    private User requireStaff(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        HttpSession session = req.getSession(false);
        Object value =
            session == null ? null : session.getAttribute("loggedInUser");
        User u = value instanceof User ? (User) value : null;
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (!isStaffRole(u.getRoleId())) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền truy cập"
            );
            return null;
        }
        return u;
    }

    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "";
        int at = email.indexOf('@');
        if (at < 0) return "******";
        String local = email.substring(0, at);
        String visible = local.length() >= 2 ? local.substring(0, 2) : local;
        return visible + "******" + email.substring(at);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        if (phone.length() <= 6) return "*".repeat(phone.length());
        return (
            phone.substring(0, 2) +
            "*".repeat(phone.length() - 6) +
            phone.substring(phone.length() - 4)
        );
    }

    private static String normalizeCode(String raw) {
        if (raw == null) return null;
        String c = raw.trim().toUpperCase();
        if (c.isEmpty()) return null;
        if (!c.startsWith("#")) c = "#" + c;
        return c.length() > 20 ? null : c;
    }

    private static String csrfToken(HttpSession session) {
        String t = (String) session.getAttribute("csrfToken");
        if (t == null) {
            t = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", t);
        }
        return t;
    }

    private static void moveFlash(
        HttpSession s,
        HttpServletRequest req,
        String key
    ) {
        Object v = s.getAttribute(key);
        if (v != null) {
            req.setAttribute(key, v);
            s.removeAttribute(key);
        }
    }

    private static void flashAndBack(
        HttpSession s,
        HttpServletResponse resp,
        HttpServletRequest req,
        String code,
        String key,
        String msg
    ) throws IOException {
        s.setAttribute(key, msg);
        String q =
            code == null
                ? ""
                : "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/staff/rentals" + q);
    }
}
