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
import model.EpisodeServer;
import model.MovieEpisode;

/**
 * Owner: Tuấn Anh / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/EpisodeDAO.java.
 */
public class EpisodeDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        EpisodeDAO.class.getName()
    );

    /**
     * Danh sách tập của một phim, sắp theo số tập tăng dần.
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public List<MovieEpisode> getEpisodesByMovieId(int movieId) {
        List<MovieEpisode> list = new ArrayList<>();
        String sql =
            "SELECT EpisodeId, MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds, CreatedAt " +
            "FROM MovieEpisodes WHERE MovieId = ? ORDER BY EpisodeNumber";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEpisode(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách tập của phim " + movieId,
                e
            );
        }
        return list;
    }

    /**
     * Lấy một tập theo id, null nếu không có.
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public MovieEpisode getEpisodeById(int episodeId) {
        String sql =
            "SELECT EpisodeId, MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds, CreatedAt " +
            "FROM MovieEpisodes WHERE EpisodeId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, episodeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEpisode(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tập " + episodeId, e);
        }
        return null;
    }

    /**
     * Lấy một tập theo MovieId và số tập (EpisodeNumber).
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public MovieEpisode getEpisodeByNumber(int movieId, int episodeNumber) {
        String sql =
            "SELECT TOP 1 EpisodeId, MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds, CreatedAt " +
            "FROM MovieEpisodes WHERE MovieId = ? AND EpisodeNumber = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            ps.setInt(2, episodeNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEpisode(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy tập theo số tập: movieId=" +
                    movieId +
                    ", ep=" +
                    episodeNumber,
                e
            );
        }
        return null;
    }

    /**
     * Lấy MovieId của tập (dùng cho API ping, tránh tin vào tham số client).
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public int getMovieIdByEpisodeId(int episodeId) {
        MovieEpisode ep = getEpisodeById(episodeId);
        return ep == null ? 0 : ep.getMovieId();
    }

    /**
     * Danh sách nguồn phát của tập, ưu tiên Priority thấp trước.
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public List<EpisodeServer> getServersByEpisodeId(int episodeId) {
        List<EpisodeServer> list = new ArrayList<>();
        String sql =
            "SELECT ServerId, EpisodeId, ServerName, ServerType, Quality, VideoUrl, SubtitleUrl, " +
            "AudioType, IsVipServer, Priority FROM EpisodeServers " +
            "WHERE EpisodeId = ? ORDER BY Priority, ServerId";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, episodeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EpisodeServer s = new EpisodeServer();
                    s.setServerId(rs.getInt("ServerId"));
                    s.setEpisodeId(rs.getInt("EpisodeId"));
                    s.setServerName(rs.getString("ServerName"));
                    s.setServerType(rs.getString("ServerType"));
                    s.setQuality(rs.getString("Quality"));
                    s.setVideoUrl(
                        normalizePlayableUrl(
                            rs.getString("VideoUrl"),
                            rs.getString("ServerType")
                        )
                    );
                    s.setSubtitleUrl(rs.getString("SubtitleUrl"));
                    s.setAudioType(rs.getString("AudioType"));
                    s.setVipServer(rs.getBoolean("IsVipServer"));
                    s.setPriority(rs.getInt("Priority"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy nguồn phát của tập " + episodeId,
                e
            );
        }

        // Dự phòng an toàn: nếu tập phim chưa cấu hình server trong CSDL, tạo sẵn 3 nguồn phát để xem trực tiếp
        if (list.isEmpty()) {
            list.add(
                buildDefaultServer(
                    1,
                    episodeId,
                    "Server VIP #1 (HLS)",
                    "HLS_SPEED",
                    "1080P_60FPS",
                    "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    "Thuyết minh",
                    false,
                    1
                )
            );
            list.add(
                buildDefaultServer(
                    2,
                    episodeId,
                    "Server Dự Phòng #2 (HLS)",
                    "HLS_BACKUP",
                    "1080P_60FPS",
                    "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    "Lồng tiếng",
                    false,
                    2
                )
            );
            list.add(
                buildDefaultServer(
                    3,
                    episodeId,
                    "Dự Phòng #3 (YouTube Embed)",
                    "IFRAME_EMBED",
                    "1080P_60FPS",
                    "https://www.youtube.com/embed/aqz-KE-bpKQ",
                    "Vietsub",
                    false,
                    3
                )
            );
        }
        return list;
    }

    /**
     * Chuẩn hóa link YouTube watch/short sang dạng /embed/ để nhúng trực tiếp
     * trên web.
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public static String normalizePlayableUrl(
        String rawUrl,
        String serverType
    ) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
        }
        String url = rawUrl.trim();
        if (url.contains("youtube.com/watch?v=")) {
            String videoId = url.substring(url.indexOf("v=") + 2);
            int amp = videoId.indexOf('&');
            if (amp != -1) {
                videoId = videoId.substring(0, amp);
            }
            return "https://www.youtube.com/embed/" + videoId;
        }
        if (url.contains("youtu.be/")) {
            String videoId = url.substring(url.lastIndexOf('/') + 1);
            int q = videoId.indexOf('?');
            if (q != -1) {
                videoId = videoId.substring(0, q);
            }
            return "https://www.youtube.com/embed/" + videoId;
        }
        return url;
    }

    private EpisodeServer buildDefaultServer(
        int id,
        int episodeId,
        String name,
        String type,
        String quality,
        String videoUrl,
        String audioType,
        boolean vipServer,
        int priority
    ) {
        EpisodeServer s = new EpisodeServer();
        s.setServerId(id);
        s.setEpisodeId(episodeId);
        s.setServerName(name);
        s.setServerType(type);
        s.setQuality(quality);
        s.setVideoUrl(videoUrl);
        s.setAudioType(audioType);
        s.setVipServer(vipServer);
        s.setPriority(priority);
        return s;
    }

    private MovieEpisode mapEpisode(ResultSet rs) throws SQLException {
        MovieEpisode ep = new MovieEpisode();
        ep.setEpisodeId(rs.getInt("EpisodeId"));
        ep.setMovieId(rs.getInt("MovieId"));
        ep.setEpisodeNumber(rs.getInt("EpisodeNumber"));
        ep.setEpisodeTitle(rs.getString("EpisodeTitle"));
        ep.setDurationSeconds(rs.getInt("DurationSeconds"));
        ep.setCreatedAt(rs.getTimestamp("CreatedAt"));
        return ep;
    }

    /**
     * Thêm tập phim mới (phục vụ AdminEpisodeController).
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public boolean addEpisode(
        int movieId,
        int episodeNumber,
        String title,
        int durationSeconds
    ) {
        String sql =
            "INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds, CreatedAt) " +
            "VALUES (?, ?, ?, ?, GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            ps.setInt(2, episodeNumber);
            ps.setString(3, title);
            ps.setInt(4, Math.max(0, durationSeconds));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi thêm tập phim mới cho movieId=" + movieId,
                e
            );
        }
        return false;
    }

    /**
     * Thêm nguồn phát máy chủ cho tập phim (phục vụ AdminEpisodeController).
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public boolean saveServer(EpisodeServer s) {
        String sql =
            "INSERT INTO EpisodeServers (EpisodeId, ServerName, ServerType, Quality, VideoUrl, SubtitleUrl, AudioType, IsVipServer, Priority) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, s.getEpisodeId());
            ps.setString(2, s.getServerName());
            ps.setString(3, s.getServerType());
            ps.setString(
                4,
                s.getQuality() != null ? s.getQuality() : "1080P_60FPS"
            );
            ps.setString(
                5,
                normalizePlayableUrl(s.getVideoUrl(), s.getServerType())
            );
            ps.setString(6, s.getSubtitleUrl());
            ps.setString(
                7,
                s.getAudioType() != null ? s.getAudioType() : "Thuyết minh"
            );
            ps.setBoolean(8, s.isVipServer());
            ps.setInt(9, s.getPriority() > 0 ? s.getPriority() : 1);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi thêm server phát cho episodeId=" + s.getEpisodeId(),
                e
            );
        }
        return false;
    }

    /**
     * Xóa tập phim.
     * Owner: Tuấn Anh / Hữu Lộc.
     */
    public boolean deleteEpisode(int episodeId) throws SQLException {
        String sql = "DELETE FROM MovieEpisodes WHERE EpisodeId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, episodeId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: retrieve servers by movie id. */
    public List<EpisodeServer> getServersByMovieId(int movieId)
        throws SQLException {
        List<EpisodeServer> a = new ArrayList<>();
        String q =
            "SELECT s.* FROM EpisodeServers s JOIN MovieEpisodes e ON s.EpisodeId=e.EpisodeId WHERE e.MovieId=? ORDER BY e.EpisodeNumber,s.Priority";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            p.setInt(1, movieId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    a.add(mapServer(r));
                }
            }
        }
        return a;
    }

    private EpisodeServer mapServer(ResultSet r) throws SQLException {
        EpisodeServer s = new EpisodeServer();
        s.setServerId(r.getInt("ServerId"));
        s.setEpisodeId(r.getInt("EpisodeId"));
        s.setServerName(r.getString("ServerName"));
        s.setServerType(r.getString("ServerType"));
        s.setQuality(r.getString("Quality"));
        s.setVideoUrl(r.getString("VideoUrl"));
        s.setSubtitleUrl(r.getString("SubtitleUrl"));
        s.setAudioType(r.getString("AudioType"));
        s.setVipServer(r.getBoolean("IsVipServer"));
        s.setPriority(r.getInt("Priority"));
        return s;
    }

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: add episode. */
    public void addEpisode(MovieEpisode e) throws SQLException {
        String q =
            "INSERT INTO MovieEpisodes(MovieId,EpisodeNumber,EpisodeTitle,DurationSeconds) VALUES(?,?,?,?)";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            p.setInt(1, e.getMovieId());
            p.setInt(2, e.getEpisodeNumber());
            p.setString(3, e.getEpisodeTitle());
            p.setInt(4, e.getDurationSeconds());
            if (p.executeUpdate() != 1) {
                throw new SQLException("Không tạo được tập phim");
            }
        }
    }

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: add server. */
    public void addServer(EpisodeServer s) throws SQLException {
        String q =
            "INSERT INTO EpisodeServers(EpisodeId,ServerName,ServerType,Quality,VideoUrl,SubtitleUrl,AudioType,IsVipServer,Priority) VALUES(?,?,?,?,?,?,?,?,?)";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            p.setInt(1, s.getEpisodeId());
            p.setString(2, s.getServerName());
            p.setString(3, s.getServerType());
            p.setString(4, s.getQuality());
            p.setString(5, s.getVideoUrl());
            p.setString(6, s.getSubtitleUrl());
            p.setString(7, s.getAudioType());
            p.setBoolean(8, s.isVipServer());
            p.setInt(9, s.getPriority());
            if (p.executeUpdate() != 1) {
                throw new SQLException("Không tạo được nguồn phát");
            }
        }
    }

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: delete server. */
    public boolean deleteServer(int serverId, int movieId) throws SQLException {
        String sql =
            "DELETE s FROM EpisodeServers s JOIN MovieEpisodes e ON e.EpisodeId=s.EpisodeId " +
            "WHERE s.ServerId=? AND e.MovieId=?";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setInt(1, serverId);
            p.setInt(2, movieId);
            return p.executeUpdate() == 1;
        }
    }
}
