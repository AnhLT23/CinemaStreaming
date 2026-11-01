package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Banner.java.
 */
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    private int bannerId;
    private String title;
    private String imageUrl;
    private String targetUrl;
    private String position;
    private String description;
    private Integer movieId;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean isActive;
    private Timestamp createdAt;
    private String movieTitle;

    public Banner() {}

    public Banner(
        int bannerId,
        String title,
        String imageUrl,
        String targetUrl,
        String position,
        Integer movieId,
        Timestamp startDate,
        Timestamp endDate,
        boolean isActive,
        Timestamp createdAt
    ) {
        this.bannerId = bannerId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.position = position;
        this.movieId = movieId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getBannerId() {
        return bannerId;
    }

    public void setBannerId(int bannerId) {
        this.bannerId = bannerId;
    }

    public int getId() {
        return bannerId;
    }

    public void setId(int id) {
        this.bannerId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imageUrl;
    }

    public void setImagePath(String imagePath) {
        this.imageUrl = imagePath;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getStartsAt() {
        return startDate;
    }

    public void setStartsAt(Timestamp startsAt) {
        this.startDate = startsAt;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Timestamp getEndsAt() {
        return endDate;
    }

    public void setEndsAt(Timestamp endsAt) {
        this.endDate = endsAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
