package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Tuấn Anh.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/WatchHistory.java.
 */
public class WatchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private long historyId;
    private int userId;
    private int episodeId;
    private int lastPosition;
    private int durationSeconds;
    private boolean isCompleted;
    private Timestamp updatedAt;
    private int movieId;
    private int episodeNumber;
    private String movieTitle;

    public WatchHistory() {}

    public WatchHistory(
        long historyId,
        int userId,
        int episodeId,
        int lastPosition,
        int durationSeconds,
        boolean isCompleted,
        Timestamp updatedAt
    ) {
        this.historyId = historyId;
        this.userId = userId;
        this.episodeId = episodeId;
        this.lastPosition = lastPosition;
        this.durationSeconds = durationSeconds;
        this.isCompleted = isCompleted;
        this.updatedAt = updatedAt;
    }

    public long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    public long getId() {
        return historyId;
    }

    public void setId(long id) {
        this.historyId = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getProgressPercent() {
        if (durationSeconds <= 0) return 0;
        return (int) Math.min(
            100,
            Math.round(((double) lastPosition / durationSeconds) * 100)
        );
    }

    public int getCompletionPercent() {
        return getProgressPercent();
    }
}
