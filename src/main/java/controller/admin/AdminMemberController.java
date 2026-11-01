package controller.admin;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import ultil.SecurityUtil;

/**
 * /admin/members - chỉ ADMIN, có lọc / phân trang / che email / khóa-mở
 * CUSTOMER.
 */
/**
 * Owner: Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminMemberController.java.
 */
@WebServlet("/admin/members")
public class AdminMemberController extends HttpServlet {

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("loggedInUser");
        if (!me.isAdmin()) {
            resp.sendError(403);
            return;
        }
        String keyword = req.getParameter("q");
        if (keyword == null) {
            keyword = "";
        }
        if (keyword.length() > 60) {
            keyword = keyword.substring(0, 60);
        }
        String status = req.getParameter("status");
        if (!"ACTIVE".equals(status) && !"LOCKED".equals(status)) {
            status = "ALL";
        }
        int page = 1;
        try {
            page = Integer.parseInt(req.getParameter("page"));
        } catch (Exception ignored) {}
        page = Math.min(Math.max(page, 1), 10_000);
        try {
            req.setAttribute(
                "members",
                users.getUsers(keyword, status, page, 20)
            );
            req.setAttribute("q", keyword);
            req.setAttribute("statusFilter", status);
            req.setAttribute("pageNumber", page);
            SecurityUtil.csrf(req);
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/user-manage.jsp"
            ).forward(req, resp);
        } catch (SQLException ex) {
            throw new ServletException("Không đọc được hội viên", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(403);
            return;
        }
        User me = (User) req.getSession().getAttribute("loggedInUser");
        if (!me.isAdmin()) {
            resp.sendError(403);
            return;
        }
        if (!"toggle_status".equals(req.getParameter("action"))) {
            resp.sendError(
                403,
                "Không được sửa thông tin cá nhân khách hàng tại đây"
            );
            return;
        }
        long target;
        try {
            target = Long.parseLong(req.getParameter("userId"));
        } catch (Exception ex) {
            resp.sendError(400);
            return;
        }
        if (target <= 0 || target == me.getId()) {
            resp.sendError(403);
            return;
        }
        try {
            if (!users.toggleStatus(target)) {
                resp.sendError(403, "Chỉ được khóa/mở CUSTOMER");
                return;
            }
            resp.sendRedirect(
                req.getContextPath() + "/admin/members?message=updated"
            );
        } catch (SQLException ex) {
            throw new ServletException("Lỗi cập nhật trạng thái", ex);
        }
    }
}
