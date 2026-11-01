package controller;

import dao.CommentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Comment;
import model.User;

/**
 * Owner: Chuẩn.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/CommentController.java.
 */
@WebServlet(
    name = "CommentController",
    urlPatterns = { "/comment", "/comments" }
)
public class CommentController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
        CommentController.class.getName()
    );
    private CommentDAO commentDAO;

    /** Owner: Chuẩn. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        this.commentDAO = new CommentDAO();
    }

    /** Owner: Chuẩn. Purpose: set comment dao. */
    public void setCommentDAO(CommentDAO commentDAO) {
        this.commentDAO = commentDAO;
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // CommentController chỉ tiếp nhận các yêu cầu gửi bình luận/phản hồi/xóa qua phương thức POST
        HttpSession session = request.getSession(true);
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute(
                "csrfToken",
                java.util.UUID.randomUUID().toString()
            );
        }
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra trạng thái đăng nhập của người dùng
        HttpSession session = request.getSession(false);
        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                loggedInUser = (User) session.getAttribute("user");
            }
        }

        if (loggedInUser == null) {
            // Chưa đăng nhập -> điều hướng đến trang đăng nhập
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. Xác thực CSRF Token
        String paramToken = request.getParameter("csrfToken");
        String sessionToken = null;
        if (session != null) {
            sessionToken = (String) session.getAttribute("csrfToken");
            if (sessionToken == null) {
                sessionToken = (String) session.getAttribute("CSRF_TOKEN");
            }
        }

        boolean isValidCsrf = false;
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            if (
                sessionToken != null && sessionToken.equals(paramToken.trim())
            ) {
                isValidCsrf = true;
            } else if (
                session != null &&
                session.getId() != null &&
                session.getId().equals(paramToken.trim())
            ) {
                isValidCsrf = true;
            }
        }

        if (!isValidCsrf) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Yêu cầu bị từ chối: CSRF token không hợp lệ hoặc thiếu."
            );
            return;
        }

        // 3. Phân luồng xử lý theo action (create, reply, delete)
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "create";
        } else {
            action = action.trim().toLowerCase();
        }

        try {
            switch (action) {
                case "create":
                    handleCreate(request, response, loggedInUser);
                    break;
                case "reply":
                    handleReply(request, response, loggedInUser);
                    break;
                case "delete":
                    handleDelete(request, response, loggedInUser);
                    break;
                default:
                    response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Thao tác không hợp lệ: " + action
                    );
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi xử lý thao tác bình luận: " + action,
                e
            );
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Đã có lỗi xảy ra trong quá trình xử lý bình luận."
            );
        }
    }

    /**
     * Xử lý tạo mới bình luận gốc kèm đánh giá sao.
     */
    private void handleCreate(
        HttpServletRequest request,
        HttpServletResponse response,
        User user
    ) throws IOException {
        // Nhận và kiểm tra movieId
        int movieId;
        try {
            movieId = Integer.parseInt(request.getParameter("movieId"));
            if (movieId <= 0) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã phim không hợp lệ."
                );
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã phim phải là số nguyên hợp lệ."
            );
            return;
        }

        if (!commentDAO.movieExists(movieId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Phim không tồn tại trong hệ thống."
            );
            return;
        }

        // Nhận và kiểm tra rating (1 - 10)
        int rating;
        try {
            rating = Integer.parseInt(request.getParameter("rating"));
            if (rating < 1 || rating > 10) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Đánh giá sao phải nằm trong khoảng từ 1 đến 10."
                );
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Đánh giá sao không hợp lệ."
            );
            return;
        }

        // Nhận và kiểm tra content (không rỗng, tối đa 1000 ký tự)
        String content = request.getParameter("content");
        if (content == null || content.trim().isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Nội dung bình luận không được để trống."
            );
            return;
        }
        content = content.trim();
        if (content.length() > 1000) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Nội dung bình luận không được vượt quá 1000 ký tự."
            );
            return;
        }

        boolean success = commentDAO.createComment(
            user.getUserId(),
            movieId,
            rating,
            content
        );
        if (!success) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể lưu bình luận vào hệ thống."
            );
            return;
        }
        response.sendRedirect(
            request.getContextPath() + "/movie?id=" + movieId
        );
    }

    /**
     * Xử lý trả lời (reply) bình luận cha.
     */
    private void handleReply(
        HttpServletRequest request,
        HttpServletResponse response,
        User user
    ) throws IOException {
        int movieId;
        try {
            movieId = Integer.parseInt(request.getParameter("movieId"));
            if (movieId <= 0) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã phim không hợp lệ."
                );
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã phim phải là số nguyên hợp lệ."
            );
            return;
        }

        if (!commentDAO.movieExists(movieId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Phim không tồn tại trong hệ thống."
            );
            return;
        }

        long parentId;
        try {
            parentId = Long.parseLong(request.getParameter("parentId"));
            if (parentId <= 0) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã bình luận cha không hợp lệ."
                );
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã bình luận cha phải là số hợp lệ."
            );
            return;
        }

        Comment parent = commentDAO.getCommentById(parentId);
        if (parent == null || !parent.isStatus()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Bình luận cha không tồn tại hoặc đã bị ẩn."
            );
            return;
        }

        if (parent.getMovieId() != movieId) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Không thể trả lời bình luận thuộc về bộ phim khác."
            );
            return;
        }

        String content = request.getParameter("content");
        if (content == null || content.trim().isEmpty()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Nội dung phản hồi không được để trống."
            );
            return;
        }
        content = content.trim();
        if (content.length() > 1000) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Nội dung phản hồi không được vượt quá 1000 ký tự."
            );
            return;
        }

        boolean success = commentDAO.replyToComment(
            user.getUserId(),
            movieId,
            parentId,
            content
        );
        if (!success) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể lưu phản hồi vào hệ thống."
            );
            return;
        }
        response.sendRedirect(
            request.getContextPath() + "/movie?id=" + movieId
        );
    }

    /**
     * Xử lý xóa mềm bình luận của chính người dùng.
     */
    private void handleDelete(
        HttpServletRequest request,
        HttpServletResponse response,
        User user
    ) throws IOException {
        long commentId;
        try {
            commentId = Long.parseLong(request.getParameter("commentId"));
            if (commentId <= 0) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã bình luận không hợp lệ."
                );
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã bình luận phải là số hợp lệ."
            );
            return;
        }

        int movieId;
        try {
            movieId = Integer.parseInt(request.getParameter("movieId"));
        } catch (NumberFormatException e) {
            movieId = 0;
        }

        Comment target = commentDAO.getCommentById(commentId);
        if (target == null || !target.isStatus()) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy bình luận cần xóa hoặc bình luận đã bị ẩn."
            );
            return;
        }

        // Kiểm tra quyền sở hữu bình luận
        if (target.getUserId() != user.getUserId()) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền xóa bình luận của người khác."
            );
            return;
        }

        boolean success = commentDAO.deleteOwnComment(
            commentId,
            user.getUserId()
        );
        if (!success) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Không thể xóa bình luận hoặc bạn không phải chủ sở hữu."
            );
            return;
        }

        int redirectMovieId = movieId > 0 ? movieId : target.getMovieId();
        response.sendRedirect(
            request.getContextPath() + "/movie?id=" + redirectMovieId
        );
    }
}
