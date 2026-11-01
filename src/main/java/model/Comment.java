package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Comment.java.
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private long commentId;
    private int userId;
    private int movieId;
    private Long parentId;
    private Integer rating;
    private String content;
    private boolean isEdited;
    private boolean status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String username;
    private String fullName;
    private String movieTitle;

    public Comment() {}

    public Comment(
        long commentId,
        int userId,
        int movieId,
        Long parentId,
        Integer rating,
        String content,
        boolean isEdited,
        boolean status,
        Timestamp createdAt,
        Timestamp updatedAt
    ) {
        this.commentId = commentId;
        this.userId = userId;
        this.movieId = movieId;
        this.parentId = parentId;
        this.rating = rating;
        this.content = content;
        this.isEdited = isEdited;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public long getId() {
        return commentId;
    }

    public void setId(long id) {
        this.commentId = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isVisible() {
        return status;
    }

    public void setVisible(boolean visible) {
        this.status = visible;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    private java.util.List<Comment> replies = new java.util.ArrayList<>();

    public java.util.List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(java.util.List<Comment> replies) {
        this.replies = replies;
    }
}
