package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Genre;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/GenreDAO.java.
 */
public class GenreDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        GenreDAO.class.getName()
    );

    /**
     * Lấy toàn bộ danh sách thể loại từ bảng Genres.
     * Ánh xạ đúng GenreId, GenreName, Slug.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public List<Genre> getAllGenres() {
        List<Genre> list = new ArrayList<>();
        String sql =
            "SELECT GenreId, GenreName, Slug FROM Genres ORDER BY GenreId ASC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setGenreId(rs.getInt("GenreId"));
                genre.setGenreName(rs.getString("GenreName"));
                genre.setSlug(rs.getString("Slug"));
                list.add(genre);
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách thể loại từ Genres",
                e
            );
        }
        return list;
    }

    /**
     * Lấy chi tiết một thể loại theo GenreId.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public Genre getGenreById(int genreId) {
        String sql =
            "SELECT GenreId, GenreName, Slug FROM Genres WHERE GenreId = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, genreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Genre genre = new Genre();
                    genre.setGenreId(rs.getInt("GenreId"));
                    genre.setGenreName(rs.getString("GenreName"));
                    genre.setSlug(rs.getString("Slug"));
                    return genre;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy thể loại theo GenreId: " + genreId,
                e
            );
        }
        return null;
    }
}
