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
import model.WatchHistory;

/**
 * Owner: Tuấn Anh.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/WatchHistoryDAO.java.
 */
public class WatchHistoryDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        WatchHistoryDAO.class.getName()
    );

    /**
     * Lấy danh sách phim đang xem dở (IsCompleted = 0) của một người dùng.
     * Kết hợp bảng MovieEpisodes và Movies để lấy đầy đủ movieId, episodeId, tên phim và tính % tiến độ.
     * Owner: Tuấn Anh.
     */
    public List<WatchHistory> getContinueWatchingByUserId(
        int userId,
        int limit
    ) {
        List<WatchHistory> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) wh.HistoryId, wh.UserId, wh.EpisodeId, wh.LastPosition, wh.DurationSeconds, " +
            "       wh.IsCompleted, wh.UpdatedAt, " +
            "       ep.MovieId, ep.EpisodeNumber, m.Title AS MovieTitle " +
            "FROM WatchHistory wh " +
            "JOIN MovieEpisodes ep ON wh.EpisodeId = ep.EpisodeId " +
            "JOIN Movies m ON ep.MovieId = m.MovieId " +
            "WHERE wh.UserId = ? AND wh.IsCompleted = 0 AND m.Status = 'PUBLISHED' " +
            "ORDER BY wh.UpdatedAt DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WatchHistory wh = new WatchHistory();
                    wh.setHistoryId(rs.getLong("HistoryId"));
                    wh.setUserId(rs.getInt("UserId"));
                    wh.setEpisodeId(rs.getInt("EpisodeId"));
                    wh.setLastPosition(rs.getInt("LastPosition"));
                    wh.setDurationSeconds(rs.getInt("DurationSeconds"));
                    wh.setCompleted(rs.getBoolean("IsCompleted"));
                    wh.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    wh.setMovieId(rs.getInt("MovieId"));
                    wh.setEpisodeNumber(rs.getInt("EpisodeNumber"));
                    wh.setMovieTitle(rs.getString("MovieTitle"));
                    list.add(wh);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách xem dở của userId: " + userId,
                e
            );
        }
        return list;
    }

    public List<WatchHistory> getRecentByUserId(int userId, int limit) {
        List<WatchHistory> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) wh.HistoryId, wh.UserId, wh.EpisodeId, wh.LastPosition, wh.DurationSeconds, " +
            "       wh.IsCompleted, wh.UpdatedAt, ep.MovieId, ep.EpisodeNumber, m.Title AS MovieTitle " +
            "FROM WatchHistory wh " +
            "JOIN MovieEpisodes ep ON wh.EpisodeId = ep.EpisodeId " +
            "JOIN Movies m ON ep.MovieId = m.MovieId " +
            "WHERE wh.UserId = ? AND m.Status = 'PUBLISHED' " +
            "ORDER BY wh.UpdatedAt DESC, wh.HistoryId DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, Math.max(1, Math.min(limit, 100)));
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WatchHistory wh = new WatchHistory();
                    wh.setHistoryId(rs.getLong("HistoryId"));
                    wh.setUserId(rs.getInt("UserId"));
                    wh.setEpisodeId(rs.getInt("EpisodeId"));
                    wh.setLastPosition(rs.getInt("LastPosition"));
                    wh.setDurationSeconds(rs.getInt("DurationSeconds"));
                    wh.setCompleted(rs.getBoolean("IsCompleted"));
                    wh.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    wh.setMovieId(rs.getInt("MovieId"));
                    wh.setEpisodeNumber(rs.getInt("EpisodeNumber"));
                    wh.setMovieTitle(rs.getString("MovieTitle"));
                    list.add(wh);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy lịch sử xem của userId: " + userId,
                e
            );
        }
        return list;
    }

    /** Mốc xem dở của user ở một tập; null nếu chưa có.
     * Owner: Tuấn Anh.
     */
    public WatchHistory getByUserAndEpisode(int userId, int episodeId) {
        String sql =
            "SELECT HistoryId, UserId, EpisodeId, LastPosition, DurationSeconds, IsCompleted, UpdatedAt " +
            "FROM WatchHistory WHERE UserId = ? AND EpisodeId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, episodeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WatchHistory wh = new WatchHistory();
                    wh.setHistoryId(rs.getLong("HistoryId"));
                    wh.setUserId(rs.getInt("UserId"));
                    wh.setEpisodeId(rs.getInt("EpisodeId"));
                    wh.setLastPosition(rs.getInt("LastPosition"));
                    wh.setDurationSeconds(rs.getInt("DurationSeconds"));
                    wh.setCompleted(rs.getBoolean("IsCompleted"));
                    wh.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    return wh;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy mốc xem dở", e);
        }
        return null;
    }

    /**
     * Upsert mốc xem dở (UQ UserId+EpisodeId). Đạt >= 95% thời lượng thì đánh dấu hoàn thành (ẩn khỏi "xem tiếp").
     * Owner: Tuấn Anh.
     */
    public boolean upsert(
        int userId,
        int episodeId,
        int positionSeconds,
        int durationSeconds
    ) {
        if (positionSeconds < 0) positionSeconds = 0;
        if (durationSeconds < 0) durationSeconds = 0;
        boolean completed =
            durationSeconds > 0 &&
            positionSeconds * 100L >= durationSeconds * 95L;
        String sql =
            "MERGE WatchHistory AS t " +
            "USING (SELECT ? AS UserId, ? AS EpisodeId) AS s " +
            "ON t.UserId = s.UserId AND t.EpisodeId = s.EpisodeId " +
            "WHEN MATCHED THEN UPDATE SET LastPosition = ?, " +
            "  DurationSeconds = CASE WHEN ? > 0 THEN ? ELSE t.DurationSeconds END, " +
            "  IsCompleted = ?, UpdatedAt = GETDATE() " +
            "WHEN NOT MATCHED THEN INSERT (UserId, EpisodeId, LastPosition, DurationSeconds, IsCompleted, UpdatedAt) " +
            "  VALUES (s.UserId, s.EpisodeId, ?, ?, ?, GETDATE());";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            int i = 1;
            ps.setInt(i++, userId);
            ps.setInt(i++, episodeId);
            ps.setInt(i++, positionSeconds);
            ps.setInt(i++, durationSeconds);
            ps.setInt(i++, durationSeconds);
            ps.setBoolean(i++, completed);
            ps.setInt(i++, positionSeconds);
            ps.setInt(i++, durationSeconds);
            ps.setBoolean(i, completed);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu mốc xem dở", e);
        }
        return false;
    }
}
