package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Movie.java.
 */
public class Movie implements Serializable {

    private static final long serialVersionUID = 1L;

    private int movieId;
    private String title;
    private String slug;
    private String originalTitle;
    private String description;
    private int duration;
    private Integer releaseYear;
    private String country;
    private String director;
    private String actors;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private String quality;
    private String audio;
    private String genres;
    private String movieType;
    private String ageRating;
    private String audioSummary;
    private int currentEpisode;
    private int totalEpisodes;
    private long viewCount;
    private double avgRating;
    private int totalReviews;
    private boolean isVipOnly;
    private boolean isRentOnly;
    private BigDecimal rentalPrice = BigDecimal.ZERO;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getId() {
        return movieId;
    }

    public void setId(int id) {
        this.movieId = id;
    }

    public Movie() {}

    public Movie(
        int movieId,
        String title,
        String originalTitle,
        String description,
        int duration,
        Integer releaseYear,
        String country,
        String director,
        String actors,
        String posterUrl,
        String backdropUrl,
        String trailerUrl,
        String movieType,
        int currentEpisode,
        int totalEpisodes,
        long viewCount,
        double avgRating,
        int totalReviews,
        boolean isVipOnly,
        boolean isRentOnly,
        double rentalPrice,
        String status,
        Timestamp createdAt,
        Timestamp updatedAt
    ) {
        this.movieId = movieId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.description = description;
        this.duration = duration;
        this.releaseYear = releaseYear;
        this.country = country;
        this.director = director;
        this.actors = actors;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.trailerUrl = trailerUrl;
        this.movieType = movieType;
        this.currentEpisode = currentEpisode;
        this.totalEpisodes = totalEpisodes;
        this.viewCount = viewCount;
        this.avgRating = avgRating;
        this.totalReviews = totalReviews;
        this.isVipOnly = isVipOnly;
        this.isRentOnly = isRentOnly;
        this.rentalPrice = BigDecimal.valueOf(rentalPrice);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSynopsis() {
        return description;
    }

    public void setSynopsis(String synopsis) {
        this.description = synopsis;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDurationMinutes() {
        return duration;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.duration = durationMinutes;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getCast() {
        return actors;
    }

    public void setCast(String cast) {
        this.actors = cast;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterPath() {
        return posterUrl;
    }

    public void setPosterPath(String posterPath) {
        this.posterUrl = posterPath;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public void setBackdropUrl(String backdropUrl) {
        this.backdropUrl = backdropUrl;
    }

    public String getBackdropPath() {
        return backdropUrl;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropUrl = backdropPath;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getAudio() {
        return audio != null ? audio : "Vietsub";
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getMovieType() {
        return movieType;
    }

    public void setMovieType(String movieType) {
        this.movieType = movieType;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getAudioSummary() {
        return audioSummary;
    }

    public void setAudioSummary(String audioSummary) {
        this.audioSummary = audioSummary;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public void setCurrentEpisode(int currentEpisode) {
        this.currentEpisode = currentEpisode;
    }

    public int getTotalEpisodes() {
        return totalEpisodes;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public boolean isVipOnly() {
        return isVipOnly;
    }

    public void setVipOnly(boolean isVipOnly) {
        this.isVipOnly = isVipOnly;
    }

    public boolean isVip() {
        return isVipOnly;
    }

    public boolean isTheatrical() {
        return isRentOnly;
    }

    public void setTheatrical(boolean theatrical) {
        this.isRentOnly = theatrical;
    }

    public boolean isRentOnly() {
        return isRentOnly;
    }

    public void setRentOnly(boolean isRentOnly) {
        this.isRentOnly = isRentOnly;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal rentalPrice) {
        this.rentalPrice = rentalPrice == null ? BigDecimal.ZERO : rentalPrice;
    }

    public void setRentalPrice(double rentalPrice) {
        this.rentalPrice = BigDecimal.valueOf(rentalPrice);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return status != null && status.equalsIgnoreCase("DELETED");
    }

    public void setDeleted(boolean deleted) {
        if (deleted) {
            status = "DELETED";
        } else if (isDeleted()) {
            status = "ACTIVE";
        }
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
}
