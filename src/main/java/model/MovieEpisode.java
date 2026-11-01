package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Owner: Tuấn Anh.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/MovieEpisode.java.
 */
public class MovieEpisode implements Serializable {

    private static final long serialVersionUID = 1L;

    private int episodeId;
    private int movieId;
    private int episodeNumber;
    private String episodeTitle;
    private int durationSeconds;
    private Timestamp createdAt;
    private List<EpisodeServer> servers = new ArrayList<>();

    public MovieEpisode() {}

    public MovieEpisode(
        int episodeId,
        int movieId,
        int episodeNumber,
        String episodeTitle,
        int durationSeconds,
        Timestamp createdAt
    ) {
        this.episodeId = episodeId;
        this.movieId = movieId;
        this.episodeNumber = episodeNumber;
        this.episodeTitle = episodeTitle;
        this.durationSeconds = durationSeconds;
        this.createdAt = createdAt;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public int getId() {
        return episodeId;
    }

    public void setId(int id) {
        this.episodeId = id;
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

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public String getTitle() {
        return episodeTitle;
    }

    public void setTitle(String title) {
        this.episodeTitle = title;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<EpisodeServer> getServers() {
        return servers;
    }

    public void setServers(List<EpisodeServer> servers) {
        this.servers =
            servers == null ? new ArrayList<>() : new ArrayList<>(servers);
    }
}
