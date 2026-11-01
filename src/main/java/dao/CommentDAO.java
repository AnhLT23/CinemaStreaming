package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Comment;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/CommentDAO.java.
 */
public class CommentDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        CommentDAO.class.getName()
    );

    /**
     * Kiểm tra phim có tồn tại trong hệ thống hay không.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean movieExists(int movieId) {
        String sql = "SELECT 1 FROM Movies WHERE MovieId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi kiểm tra phim tồn tại: " + movieId,
                e
            );
        }
        return false;
    }

    /**
     * Thêm bình luận gốc mới kèm đánh giá sao (1-10 sao).
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean createComment(
        int userId,
        int movieId,
        int rating,
        String content
    ) {
        String sql =
            "INSERT INTO Comments (UserId, MovieId, ParentId, Rating, Content, IsEdited, Status, CreatedAt, UpdatedAt) " +
            "VALUES (?, ?, NULL, ?, ?, 0, 1, GETDATE(), GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setInt(3, rating);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo bình luận gốc", e);
        }
        return false;
    }

    /**
     * Thêm phản hồi (reply) cho bình luận cha, rating là NULL.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean replyToComment(
        int userId,
        int movieId,
        long parentId,
        String content
    ) {
        String sql =
            "INSERT INTO Comments (UserId, MovieId, ParentId, Rating, Content, IsEdited, Status, CreatedAt, UpdatedAt) " +
            "VALUES (?, ?, ?, NULL, ?, 0, 1, GETDATE(), GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setLong(3, parentId);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi phản hồi bình luận: " + parentId,
                e
            );
        }
        return false;
    }

    /**
     * Soft delete bình luận của chính người dùng bằng Status = 0. Kiểm tra
     * quyền sở hữu ngay trong câu UPDATE để đảm bảo an toàn tuyệt đối ở tầng cơ
     * sở dữ liệu.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean deleteOwnComment(long commentId, int userId) {
        String sql =
            "UPDATE Comments SET Status = 0, UpdatedAt = GETDATE() " +
            "WHERE CommentId = ? AND UserId = ? AND Status = 1";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, commentId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa mềm bình luận sở hữu", e);
        }
        return false;
    }

    /**
     * Lấy thông tin một bình luận theo CommentId.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public Comment getCommentById(long commentId) {
        String sql =
            "SELECT c.CommentId, c.UserId, c.MovieId, c.ParentId, c.Rating, c.Content, " +
            "       c.IsEdited, c.Status, c.CreatedAt, c.UpdatedAt, u.Username " +
            "FROM Comments c " +
            "LEFT JOIN Users u ON c.UserId = u.UserId " +
            "WHERE c.CommentId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy chi tiết bình luận: " + commentId,
                e
            );
        }
        return null;
    }

    /**
     * Lấy danh sách bình luận đang hiển thị (Status = 1) của phim, tổ chức theo
     * cấu trúc đa cấp (gốc & replies).
     * Owner: Chuẩn / Hữu Lộc.
     */
    public List<Comment> getCommentsByMovieId(int movieId) {
        List<Comment> allComments = new ArrayList<>();
        String sql =
            "SELECT c.CommentId, c.UserId, c.MovieId, c.ParentId, c.Rating, c.Content, " +
            "       c.IsEdited, c.Status, c.CreatedAt, c.UpdatedAt, u.Username " +
            "FROM Comments c " +
            "LEFT JOIN Users u ON c.UserId = u.UserId " +
            "WHERE c.MovieId = ? AND c.Status = 1 " +
            "ORDER BY c.CreatedAt ASC, c.CommentId ASC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    allComments.add(mapResultSetToComment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách bình luận của phim: " + movieId,
                e
            );
        }

        Map<Long, Comment> map = new LinkedHashMap<>();
        List<Comment> rootComments = new ArrayList<>();

        for (Comment c : allComments) {
            map.put(c.getCommentId(), c);
        }

        for (Comment c : allComments) {
            if (c.getParentId() == null || c.getParentId() <= 0) {
                rootComments.add(c);
            } else {
                Comment parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.getReplies().add(c);
                }
                // Nếu bình luận cha không tồn tại hoặc đã bị ẩn (Status = 0), không đẩy reply mồ côi thành root
            }
        }

        Collections.reverse(rootComments);
        return rootComments;
    }

    /**
     * Ánh xạ kết quả truy vấn sang Model Comment.
     */
    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setCommentId(rs.getLong("CommentId"));
        c.setUserId(rs.getInt("UserId"));
        c.setMovieId(rs.getInt("MovieId"));

        long parentId = rs.getLong("ParentId");
        c.setParentId(rs.wasNull() ? null : parentId);

        int rating = rs.getInt("Rating");
        c.setRating(rs.wasNull() ? null : rating);

        c.setContent(rs.getString("Content"));
        c.setEdited(rs.getBoolean("IsEdited"));
        c.setStatus(rs.getBoolean("Status"));
        c.setCreatedAt(rs.getTimestamp("CreatedAt"));
        c.setUpdatedAt(rs.getTimestamp("UpdatedAt"));

        try {
            c.setUsername(rs.getString("Username"));
        } catch (SQLException ignored) {}

        return c;
    }

    /**
     * Lấy toàn bộ bình luận trên hệ thống cho AdminCommentController.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public List<Comment> getAllComments() {
        List<Comment> list = new ArrayList<>();
        String sql =
            "SELECT TOP 100 c.CommentId, c.UserId, c.MovieId, c.ParentId, c.Rating, c.Content, " +
            "c.IsEdited, c.Status, c.CreatedAt, c.UpdatedAt, u.Username " +
            "FROM Comments c JOIN Users u ON c.UserId = u.UserId ORDER BY c.CreatedAt DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Comment c = new Comment();
                c.setCommentId(rs.getLong("CommentId"));
                c.setUserId(rs.getInt("UserId"));
                c.setMovieId(rs.getInt("MovieId"));
                long pId = rs.getLong("ParentId");
                c.setParentId(rs.wasNull() ? null : pId);
                int rating = rs.getInt("Rating");
                c.setRating(rs.wasNull() ? null : rating);
                c.setContent(rs.getString("Content"));
                c.setEdited(rs.getBoolean("IsEdited"));
                c.setStatus(rs.getBoolean("Status"));
                c.setCreatedAt(rs.getTimestamp("CreatedAt"));
                c.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                c.setUsername(rs.getString("Username"));
                list.add(c);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy toàn bộ bình luận cho Admin", e);
        }
        return list;
    }

    /**
     * Bật/tắt trạng thái hiển thị (Status = 0/1) của bình luận (nghiêm cấm sửa
     * nội dung chữ).
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean toggleCommentStatus(long commentId) {
        String sql =
            "UPDATE Comments SET Status = CASE WHEN Status = 1 THEN 0 ELSE 1 END, UpdatedAt = GETDATE() WHERE CommentId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi ẩn/hiện commentId=" + commentId, e);
        }
        return false;
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: retrieve all. */
    public List<Comment> getAll() throws SQLException {
        return getAll(null, null);
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: retrieve all. */
    public List<Comment> getAll(String keyword, String visible)
        throws SQLException {
        List<Comment> a = new ArrayList<>();
        String q =
            "SELECT TOP 200 c.CommentId,c.UserId,c.MovieId,c.Rating,c.Content,c.Status,c.CreatedAt," +
            "u.Username,u.FullName,m.Title AS MovieTitle FROM Comments c " +
            "JOIN Users u ON c.UserId=u.UserId JOIN Movies m ON c.MovieId=m.MovieId " +
            "WHERE (? IS NULL OR c.Content LIKE ? OR u.Username LIKE ? OR u.FullName LIKE ? OR m.Title LIKE ?) " +
            "AND (? IS NULL OR c.Status = ?) " +
            "ORDER BY c.CreatedAt DESC, c.CommentId DESC";
        String search =
            keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        Boolean status = "true".equalsIgnoreCase(visible)
            ? Boolean.TRUE
            : "false".equalsIgnoreCase(visible)
              ? Boolean.FALSE
              : null;
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            p.setString(1, search);
            p.setString(2, search == null ? null : "%" + search + "%");
            p.setString(3, search == null ? null : "%" + search + "%");
            p.setString(4, search == null ? null : "%" + search + "%");
            p.setString(5, search == null ? null : "%" + search + "%");
            if (status == null) {
                p.setNull(6, java.sql.Types.BIT);
                p.setNull(7, java.sql.Types.BIT);
            } else {
                p.setBoolean(6, status);
                p.setBoolean(7, status);
            }
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Comment x = new Comment();
                    x.setCommentId(r.getLong("CommentId"));
                    x.setUserId(r.getInt("UserId"));
                    x.setMovieId(r.getInt("MovieId"));
                    int rate = r.getInt("Rating");
                    x.setRating(r.wasNull() ? null : rate);
                    x.setContent(r.getString("Content"));
                    x.setStatus(r.getBoolean("Status"));
                    x.setCreatedAt(r.getTimestamp("CreatedAt"));
                    x.setUsername(r.getString("Username"));
                    x.setFullName(r.getString("FullName"));
                    x.setMovieTitle(r.getString("MovieTitle"));
                    a.add(x);
                }
            }
        }
        return a;
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: update status. */
    public boolean updateStatus(long id, boolean status) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "UPDATE Comments SET Status=?, UpdatedAt=GETDATE() WHERE CommentId=?"
            )
        ) {
            p.setBoolean(1, status);
            p.setLong(2, id);
            return p.executeUpdate() > 0;
        }
    }
}
