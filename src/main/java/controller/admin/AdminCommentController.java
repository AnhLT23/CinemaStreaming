package controller.admin;

import dao.CommentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ultil.SecurityUtil;

/**
 * Owner: Hữu Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminCommentController.java.
 */
@WebServlet("/admin/comments")
public class AdminCommentController extends HttpServlet {

    private final CommentDAO dao = new CommentDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            SecurityUtil.csrf(req);
            String keyword = req.getParameter("q");
            if (keyword != null && keyword.length() > 100) {
                keyword = keyword.substring(0, 100);
            }
            String visible = req.getParameter("visible");
            if (!"true".equals(visible) && !"false".equals(visible)) {
                visible = "";
            }
            req.setAttribute("q", keyword == null ? "" : keyword);
            req.setAttribute("visibleFilter", visible);
            req.setAttribute("comments", dao.getAll(keyword, visible));
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/comment-manage.jsp"
            ).forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF không hợp lệ"
            );
            return;
        }
        try {
            long id = Long.parseLong(req.getParameter("commentId"));
            if (id <= 0) {
                resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Comment ID không hợp lệ."
                );
                return;
            }
            String action = req.getParameter("action");
            boolean status;
            if ("show".equals(action)) {
                status = true;
            } else if ("hide".equals(action)) {
                status = false;
            } else {
                resp.sendError(400, "Action không hợp lệ.");
                return;
            }
            if (!dao.updateStatus(id, status)) {
                resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy bình luận."
                );
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/admin/comments");
        } catch (NumberFormatException e) {
            resp.sendError(400, "Comment ID không hợp lệ.");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
