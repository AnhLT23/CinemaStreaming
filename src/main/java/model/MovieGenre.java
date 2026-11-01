package model;

import java.io.Serializable;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/MovieGenre.java.
 */
public class MovieGenre implements Serializable {

    private int movieId;
    private int genreId;

    public MovieGenre() {}

    public MovieGenre(int movieId, int genreId) {
        this.movieId = movieId;
        this.genreId = genreId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }
}
