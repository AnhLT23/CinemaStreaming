package controller.admin;

import dao.EpisodeDAO;
import dao.MovieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.EpisodeServer;
import model.Movie;
import model.MovieEpisode;
import ultil.SecurityUtil;

/**
 * Owner: Hữu Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminEpisodeController.java.
 */
@WebServlet("/admin/episodes")
public class AdminEpisodeController extends HttpServlet {

    private final EpisodeDAO dao = new EpisodeDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            SecurityUtil.csrf(req);
            List<Movie> movies = movieDAO.getAll(null, null);
            movies.removeIf(movie ->
                "DELETED".equalsIgnoreCase(movie.getStatus())
            );
            req.setAttribute("movies", movies);
            String selectedId = req.getParameter("movieId");
            req.setAttribute("episodes", Collections.emptyList());
            if (selectedId != null && !selectedId.trim().isEmpty()) {
                int movieId = parsePositiveInt(
                    selectedId,
                    "Movie ID không hợp lệ."
                );
                Movie movie = movieDAO.getById(movieId);
                if (movie == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("selectedMovie", movie);
                List<MovieEpisode> episodes = dao.getEpisodesByMovieId(movieId);
                List<EpisodeServer> servers = dao.getServersByMovieId(movieId);
                Map<Integer, List<EpisodeServer>> serversByEpisode =
                    new HashMap<>();
                for (EpisodeServer server : servers) {
                    serversByEpisode
                        .computeIfAbsent(server.getEpisodeId(), key ->
                            new ArrayList<>()
                        )
                        .add(server);
                }
                for (MovieEpisode episode : episodes) {
                    episode.setServers(
                        serversByEpisode.getOrDefault(
                            episode.getEpisodeId(),
                            Collections.emptyList()
                        )
                    );
                }
                req.setAttribute("episodes", episodes);
            }
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/episode-manage.jsp"
            ).forward(req, resp);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể tải dữ liệu tập phim", e);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF không hợp lệ"
            );
            return;
        }
        try {
            String action = req.getParameter("action");
            int movieId = parsePositiveInt(
                req.getParameter("movieId"),
                "Movie ID không hợp lệ."
            );
            Movie movie = movieDAO.getById(movieId);
            if (movie == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if ("addEpisode".equals(action)) {
                MovieEpisode e = new MovieEpisode();
                e.setMovieId(movieId);
                e.setEpisodeNumber(
                    parsePositiveInt(
                        req.getParameter("episodeNumber"),
                        "Số tập không hợp lệ."
                    )
                );
                e.setEpisodeTitle(parameter(req, "episodeTitle"));
                e.setDurationSeconds(
                    parseNonNegativeInt(
                        req.getParameter("durationSeconds"),
                        "Thời lượng tập không hợp lệ."
                    )
                );
                if (
                    e.getEpisodeTitle().isEmpty() ||
                    e.getEpisodeTitle().length() > 200
                ) {
                    throw new IllegalArgumentException(
                        "Thông tin tập phim không hợp lệ."
                    );
                }
                dao.addEpisode(e);
            } else if ("addServer".equals(action)) {
                EpisodeServer s = new EpisodeServer();
                s.setEpisodeId(
                    parsePositiveInt(
                        req.getParameter("episodeId"),
                        "Episode ID không hợp lệ."
                    )
                );
                MovieEpisode episode = dao.getEpisodeById(s.getEpisodeId());
                if (episode == null || episode.getMovieId() != movieId) {
                    resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Tập phim không thuộc bộ phim đã chọn."
                    );
                    return;
                }
                s.setServerName(parameter(req, "serverName"));
                s.setServerType(req.getParameter("serverType"));
                s.setQuality(parameter(req, "quality"));
                s.setVideoUrl(parameter(req, "videoUrl"));
                s.setSubtitleUrl(parameter(req, "subtitleUrl"));
                s.setAudioType(parameter(req, "audioType"));
                s.setVipServer(req.getParameter("isVipServer") != null);
                s.setPriority(
                    parsePositiveInt(
                        req.getParameter("priority"),
                        "Priority không hợp lệ."
                    )
                );
                if (
                    s.getServerName().isEmpty() ||
                    s.getServerName().length() > 100 ||
                    s.getQuality().isEmpty() ||
                    s.getAudioType().isEmpty() ||
                    !isHttpUrl(s.getVideoUrl()) ||
                    s.getVideoUrl().length() > 1000 ||
                    (!s.getSubtitleUrl().isEmpty() &&
                        (!isHttpUrl(s.getSubtitleUrl()) ||
                            s.getSubtitleUrl().length() > 500)) ||
                    s.getQuality().length() > 20 ||
                    s.getAudioType().length() > 50
                ) {
                    throw new IllegalArgumentException(
                        "Tên server hoặc URL phát/phụ đề không hợp lệ."
                    );
                }
                if (
                    !java.util.Arrays.asList(
                        "HLS_SPEED",
                        "HLS_BACKUP",
                        "IFRAME_EMBED"
                    ).contains(s.getServerType())
                ) {
                    throw new IllegalArgumentException(
                        "Loại server không hợp lệ."
                    );
                }
                if (
                    !java.util.Arrays.asList(
                        "720P",
                        "1080P_60FPS",
                        "4K_UHD"
                    ).contains(s.getQuality()) ||
                    !java.util.Arrays.asList(
                        "Thuyết minh",
                        "Lồng tiếng",
                        "Vietsub"
                    ).contains(s.getAudioType())
                ) {
                    throw new IllegalArgumentException(
                        "Chất lượng hoặc loại âm thanh không hợp lệ."
                    );
                }
                dao.addServer(s);
            } else if ("deleteEpisode".equals(action)) {
                int episodeId = parsePositiveInt(
                    req.getParameter("episodeId"),
                    "Episode ID không hợp lệ."
                );
                MovieEpisode episode = dao.getEpisodeById(episodeId);
                if (episode == null || episode.getMovieId() != movieId) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (!dao.deleteEpisode(episodeId)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else if ("deleteServer".equals(action)) {
                int serverId = parsePositiveInt(
                    req.getParameter("serverId"),
                    "Server ID không hợp lệ."
                );
                if (!dao.deleteServer(serverId, movieId)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } else {
                resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Action không hợp lệ."
                );
                return;
            }
            resp.sendRedirect(
                req.getContextPath() + "/admin/episodes?movieId=" + movieId
            );
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Không thể cập nhật tập phim", e);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static String parameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static int parsePositiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(message);
    }

    private static int parseNonNegativeInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(message);
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            return (
                uri.isAbsolute() &&
                ("http".equalsIgnoreCase(uri.getScheme()) ||
                    "https".equalsIgnoreCase(uri.getScheme())) &&
                uri.getHost() != null
            );
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
