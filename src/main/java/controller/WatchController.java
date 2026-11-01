package controller;

import dao.EpisodeDAO;
import dao.IssueReportDAO;
import dao.MovieDAO;
import dao.MovieRentalDAO;
import dao.WatchHistoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import model.EpisodeServer;
import model.Movie;
import model.MovieEpisode;
import model.User;
import model.WatchHistory;
import ultil.PlaybackAccess;

/**
 * Owner: Tuấn Anh.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/WatchController.java.
 */
@WebServlet(name = "WatchController", urlPatterns = { "/watch", "/xem-phim" })
public class WatchController extends HttpServlet {

    private static final Set<String> ISSUE_TYPES = Set.of(
        "DEAD_LINK",
        "AUDIO_DESYNC",
        "WRONG_SUBTITLE",
        "VIDEO_LAG",
        "SUBTITLE_ERROR"
    );

    private MovieDAO movieDAO;
    private EpisodeDAO episodeDAO;
    private MovieRentalDAO rentalDAO;
    private WatchHistoryDAO historyDAO;
    private IssueReportDAO issueDAO;

    /** Owner: Tuấn Anh. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        movieDAO = new MovieDAO();
        episodeDAO = new EpisodeDAO();
        rentalDAO = new MovieRentalDAO();
        historyDAO = new WatchHistoryDAO();
        issueDAO = new IssueReportDAO();
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // 1. Phim
        int movieId = parseInt(
            firstNonBlank(
                request.getParameter("movieId"),
                request.getParameter("id")
            ),
            0
        );
        Movie movie = movieId > 0 ? movieDAO.getById(movieId) : null;
        if (movie == null || !"PUBLISHED".equalsIgnoreCase(movie.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = request.getSession();
        String ctx = request.getContextPath();

        // 2. Quyền xem: đăng nhập / VIP / vé rạp 48H
        PlaybackAccess.Result access = PlaybackAccess.check(
            session,
            movie,
            rentalDAO
        );
        if (access != PlaybackAccess.Result.ALLOWED) {
            denyAccess(request, response, session, movie, access, ctx);
            return;
        }
        User user = PlaybackAccess.currentUser(session);
        boolean vip =
            PlaybackAccess.isVipActive(user) || PlaybackAccess.isAdmin(session);

        // 3. Tập phim (mặc định tập đầu tiên), hỗ trợ tìm theo cả episodeId hoặc ep (số tập)
        List<MovieEpisode> episodes = episodeDAO.getEpisodesByMovieId(movieId);
        if (episodes.isEmpty()) {
            MovieEpisode fallbackEp = new MovieEpisode();
            fallbackEp.setEpisodeId(1);
            fallbackEp.setMovieId(movieId);
            fallbackEp.setEpisodeNumber(1);
            fallbackEp.setEpisodeTitle("Tập 01 - Bản Đầy Đủ");
            fallbackEp.setDurationSeconds(
                movie.getDurationMinutes() > 0
                    ? movie.getDurationMinutes() * 60
                    : 1458
            );
            episodes = new ArrayList<>();
            episodes.add(fallbackEp);
        }

        MovieEpisode episode = episodes.get(0);
        int episodeId = parseInt(request.getParameter("episodeId"), 0);
        int epNumber = parseInt(request.getParameter("ep"), 0);

        if (episodeId > 0) {
            episode = null;
            for (MovieEpisode e : episodes) {
                if (e.getEpisodeId() == episodeId) {
                    episode = e;
                    break;
                }
            }
            if (episode == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else if (epNumber > 0) {
            for (MovieEpisode e : episodes) {
                if (e.getEpisodeNumber() == epNumber) {
                    episode = e;
                    break;
                }
            }
        }

        // 4. Nguồn phát: chọn theo serverId nếu được phép, ngược lại lấy nguồn đầu tiên phù hợp quyền
        List<EpisodeServer> servers = episodeDAO.getServersByEpisodeId(
            episode.getEpisodeId()
        );
        int serverId = parseInt(request.getParameter("serverId"), 0);
        EpisodeServer selected = null;
        for (EpisodeServer s : servers) {
            if (s.getServerId() == serverId && (!s.isVipServer() || vip)) {
                selected = s;
                break;
            }
        }
        if (selected == null) {
            for (EpisodeServer s : servers) {
                if (!s.isVipServer() || vip) {
                    selected = s;
                    break;
                }
            }
        }
        if (selected == null && !servers.isEmpty()) {
            selected = servers.get(0);
        }
        if (selected == null) {
            session.setAttribute("errorMessage", "Tập này chưa có nguồn phát.");
            response.sendRedirect(ctx + "/movie?id=" + movieId);
            return;
        }

        // 5. Mốc xem dở (kiểm tra user != null an toàn)
        int startPosition = 0;
        if (user != null) {
            WatchHistory history = historyDAO.getByUserAndEpisode(
                user.getUserId(),
                episode.getEpisodeId()
            );
            if (history != null && !history.isCompleted()) {
                startPosition = history.getLastPosition();
            }
        }

        // 6. Xác định kiểu phát: file HLS/MP4 dùng <video>, còn lại (YouTube, iframe) dùng <iframe>
        String rawUrl =
            selected.getVideoUrl() == null ? "" : selected.getVideoUrl().trim();
        String url = EpisodeDAO.normalizePlayableUrl(
            rawUrl,
            selected.getServerType()
        );
        String lower = url.toLowerCase();
        boolean embedMode = !(
            lower.contains(".m3u8") ||
            lower.contains(".mp4") ||
            lower.contains(".webm")
        );
        String playUrl = url;
        if (
            embedMode &&
            startPosition > 0 &&
            lower.contains("youtube.com/embed")
        ) {
            playUrl =
                url +
                (url.contains("?") ? "&" : "?") +
                "start=" +
                startPosition;
        }
        String subtitle = selected.getSubtitleUrl();
        if (subtitle != null && subtitle.startsWith("/")) {
            subtitle = ctx + subtitle;
        }

        // 7. Tăng lượt xem một lần cho mỗi tập trong một phiên
        @SuppressWarnings("unchecked")
        Set<Integer> viewed = (Set<Integer>) session.getAttribute(
            "viewedEpisodes"
        );
        if (viewed == null) {
            viewed = new HashSet<>();
            session.setAttribute("viewedEpisodes", viewed);
        }
        if (viewed.add(episode.getEpisodeId())) {
            movieDAO.incrementViewCount(movieId);
        }

        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        // 8. Nạp danh sách phim cùng thể loại cho cột phải
        List<Movie> relatedMovies = movieDAO.getMovieList(1, 4, null);

        // Set đầy đủ cả tên biến chuẩn lẫn alias để tương thích mọi file JSP
        request.setAttribute("movie", movie);
        request.setAttribute("episode", episode);
        request.setAttribute("currentEpisode", episode);
        request.setAttribute("currentEp", episode);
        request.setAttribute("episodes", episodes);
        request.setAttribute("servers", servers);
        request.setAttribute("selectedServer", selected);
        request.setAttribute("activeServer", selected);
        request.setAttribute("embedMode", embedMode);
        request.setAttribute("playUrl", playUrl);
        request.setAttribute("subtitleUrl", subtitle);
        request.setAttribute("startPosition", startPosition);
        request.setAttribute("resumeSecond", startPosition);
        request.setAttribute("userIsVip", vip);
        request.setAttribute("relatedMovies", relatedMovies);

        request
            .getRequestDispatcher("/WEB-INF/views/client/watch.jsp")
            .forward(request, response);
    }

    /** Báo lỗi phát (action=report). */
    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = PlaybackAccess.currentUser(session);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = request.getParameter("csrfToken");
        Object expected =
            session != null ? session.getAttribute("csrfToken") : null;
        if (
            token != null &&
            expected != null &&
            !token.equals(expected.toString())
        ) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF token không hợp lệ."
            );
            return;
        }
        if (!"report".equalsIgnoreCase(request.getParameter("action"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int episodeId = parseInt(request.getParameter("episodeId"), 0);
        MovieEpisode episode =
            episodeId > 0 ? episodeDAO.getEpisodeById(episodeId) : null;
        if (episode == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String type = request.getParameter("issueType");
        if (type == null || !ISSUE_TYPES.contains(type)) {
            type = "DEAD_LINK";
        }
        String desc = request.getParameter("description");
        if (desc != null) {
            desc = desc.trim();
            if (desc.length() > 500) desc = desc.substring(0, 500);
            if (desc.isEmpty()) desc = null;
        }
        int serverId = parseInt(request.getParameter("serverId"), 0);
        boolean ok = issueDAO.createReport(
            episodeId,
            serverId > 0 ? serverId : null,
            user.getUserId(),
            type,
            desc
        );
        if (session != null) {
            session.setAttribute(
                ok ? "successMessage" : "errorMessage",
                ok
                    ? "Đã ghi nhận báo lỗi, cảm ơn bạn!"
                    : "Không gửi được báo lỗi, vui lòng thử lại."
            );
        }
        response.sendRedirect(
            request.getContextPath() +
                "/watch?movieId=" +
                episode.getMovieId() +
                "&episodeId=" +
                episodeId +
                (serverId > 0 ? "&serverId=" + serverId : "")
        );
    }

    private void denyAccess(
        HttpServletRequest request,
        HttpServletResponse response,
        HttpSession session,
        Movie movie,
        PlaybackAccess.Result access,
        String ctx
    ) throws IOException {
        int movieId = movie.getMovieId();
        switch (access) {
            case LOGIN_REQUIRED: {
                String back =
                    request.getRequestURI().substring(ctx.length()) +
                    (request.getQueryString() == null
                        ? ""
                        : "?" + request.getQueryString());
                response.sendRedirect(
                    ctx +
                        "/login?redirect=" +
                        URLEncoder.encode(back, StandardCharsets.UTF_8)
                );
                return;
            }
            case VIP_REQUIRED:
                session.setAttribute(
                    "errorMessage",
                    "Phim này dành riêng cho hội viên VIP."
                );
                response.sendRedirect(ctx + "/vip");
                return;
            case RENT_REQUIRED:
                session.setAttribute(
                    "errorMessage",
                    "Phim chiếu rạp cần thuê vé 48H để xem."
                );
                response.sendRedirect(ctx + "/vip?rentMovieId=" + movieId);
                return;
            case RENTAL_REVOKED:
                session.setAttribute(
                    "errorMessage",
                    "Vé thuê của bạn đã bị thu hồi. Vui lòng liên hệ hỗ trợ."
                );
                response.sendRedirect(ctx + "/movie?id=" + movieId);
                return;
            default:
                session.setAttribute(
                    "errorMessage",
                    "Vé thuê 48H đã hết hạn. Hãy thuê lại để tiếp tục xem."
                );
                response.sendRedirect(ctx + "/vip?rentMovieId=" + movieId);
        }
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private static int parseInt(String s, int def) {
        try {
            return s == null ? def : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
