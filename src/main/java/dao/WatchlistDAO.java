package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Watchlist;

/**
 * Owner: Lộc / Chuẩn.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/WatchlistDAO.java.
 */
public class WatchlistDAO extends DBContext {

    public List<Watchlist> getByUserId(int userId) throws SQLException {
        requirePositiveId(userId, "userId");
        String sql =
            "SELECT WatchlistId, UserId, MovieId, CreatedAt " +
            "FROM Watchlists WHERE UserId = ? ORDER BY CreatedAt DESC, WatchlistId DESC";
        List<Watchlist> watchlist = new ArrayList<>();

        try (
            Connection conn = requireConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    watchlist.add(
                        new Watchlist(
                            rs.getLong("WatchlistId"),
                            rs.getInt("UserId"),
                            rs.getInt("MovieId"),
                            rs.getTimestamp("CreatedAt")
                        )
                    );
                }
            }
        }
        return watchlist;
    }

    public boolean add(int userId, int movieId) throws SQLException {
        requirePositiveId(userId, "userId");
        requirePositiveId(movieId, "movieId");
        String sql = "INSERT INTO Watchlists (UserId, MovieId) VALUES (?, ?)";

        try (
            Connection conn = requireConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean remove(int userId, int movieId) throws SQLException {
        requirePositiveId(userId, "userId");
        requirePositiveId(movieId, "movieId");
        String sql = "DELETE FROM Watchlists WHERE UserId = ? AND MovieId = ?";

        try (
            Connection conn = requireConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean contains(int userId, int movieId) throws SQLException {
        requirePositiveId(userId, "userId");
        requirePositiveId(movieId, "movieId");
        String sql =
            "SELECT 1 FROM Watchlists WHERE UserId = ? AND MovieId = ?";

        try (
            Connection conn = requireConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException(
                "Không thể kết nối cơ sở dữ liệu để truy cập Watchlists."
            );
        }
        return conn;
    }

    private void requirePositiveId(int id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException(name + " phải lớn hơn 0.");
        }
    }
}
