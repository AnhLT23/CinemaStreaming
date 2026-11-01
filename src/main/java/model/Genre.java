package model;

import java.io.Serializable;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/Genre.java.
 */
public class Genre implements Serializable {

    private int genreId;
    private String genreName;
    private String slug;

    public Genre() {}

    public Genre(int genreId, String genreName, String slug) {
        this.genreId = genreId;
        this.genreName = genreName;
        this.slug = slug;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getId() {
        return genreId;
    }

    public void setId(int id) {
        this.genreId = id;
    }

    public String getName() {
        return genreName;
    }

    public void setName(String name) {
        this.genreName = name;
    }
}
