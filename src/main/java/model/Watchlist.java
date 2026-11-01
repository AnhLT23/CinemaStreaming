package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Lộc / Chuẩn.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Watchlist.java.
 */
public class Watchlist implements Serializable {

    private long watchlistId;
    private int userId;
    private int movieId;
    private Timestamp createdAt;

    public Watchlist() {}

    public Watchlist(
        long watchlistId,
        int userId,
        int movieId,
        Timestamp createdAt
    ) {
        this.watchlistId = watchlistId;
        this.userId = userId;
        this.movieId = movieId;
        this.createdAt = createdAt;
    }

    public long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(long watchlistId) {
        this.watchlistId = watchlistId;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
