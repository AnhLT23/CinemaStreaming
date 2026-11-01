package controller.api;

import dao.EpisodeDAO;
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
import model.Movie;
import model.MovieEpisode;
import model.User;
import ultil.PlaybackAccess;

/**
 * Heartbeat 10s từ trình phát: lưu mốc giây xem dở. Trả 403 nếu vé thuê đã bị thu hồi/hết hạn
 * để player phía client tự dừng video.
 */
/**
 * Owner: Tuấn Anh.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/api/WatchHistoryAPI.java.
 */
@WebServlet(name = "WatchHistoryAPI", urlPatterns = { "/api/history/sync" })
public class WatchHistoryAPI extends HttpServlet {

    private EpisodeDAO episodeDAO;
    private MovieDAO movieDAO;
    private MovieRentalDAO rentalDAO;
    private WatchHistoryDAO historyDAO;

    /** Owner: Tuấn Anh. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        episodeDAO = new EpisodeDAO();
        movieDAO = new MovieDAO();
        rentalDAO = new MovieRentalDAO();
        historyDAO = new WatchHistoryDAO();
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        User user = PlaybackAccess.currentUser(session);
        if (user == null) {
            write(response, 401, "unauthorized", "Vui lòng đăng nhập.");
            return;
        }

        String token = request.getHeader("X-CSRF-Token");
        if (token == null) {
            token = request.getParameter("csrfToken");
        }
        Object expected =
            session != null ? session.getAttribute("csrfToken") : null;
        if (
            expected != null &&
            token != null &&
            !token.equals(expected.toString())
        ) {
            write(response, 403, "csrf", "CSRF token không hợp lệ.");
            return;
        }

        int episodeId = parseInt(request.getParameter("episodeId"));
        int position = parseFirstValidInt(
            request.getParameter("positionSeconds"),
            request.getParameter("lastPosition"),
            request.getParameter("currentSecond")
        );
        int duration = parseFirstValidInt(
            request.getParameter("durationSeconds"),
            request.getParameter("duration"),
            "1458"
        );
        if (episodeId <= 0 || position < 0 || duration < 0) {
            write(response, 400, "bad_request", "Tham số không hợp lệ.");
            return;
        }

        // Không tin movieId từ client: suy ra từ tập phim
        MovieEpisode episode = episodeDAO.getEpisodeById(episodeId);
        Movie movie =
            episode == null ? null : movieDAO.getById(episode.getMovieId());
        if (movie == null) {
            write(response, 404, "not_found", "Không tìm thấy tập phim.");
            return;
        }

        // Kiểm tra quyền xem & trạng thái vé bị Staff thu hồi (REVOKED)
        PlaybackAccess.Result access = PlaybackAccess.check(
            session,
            movie,
            rentalDAO
        );
        if (
            access != PlaybackAccess.Result.ALLOWED ||
            rentalDAO.isRentalRevokedForEpisode(user.getUserId(), episodeId)
        ) {
            String code =
                access == PlaybackAccess.Result.RENTAL_REVOKED ||
                rentalDAO.isRentalRevokedForEpisode(user.getUserId(), episodeId)
                    ? "revoked"
                    : access == PlaybackAccess.Result.RENTAL_EXPIRED
                      ? "expired"
                      : "forbidden";
            String msg = "revoked".equals(code)
                ? "Vé xem của bạn đã bị thu hồi. Video sẽ dừng phát."
                : "Bạn không còn quyền xem nội dung này.";
            write(response, 403, code, msg);
            return;
        }

        // Giới hạn mốc không vượt quá thời lượng (nếu client gửi), tránh dữ liệu rác
        if (duration > 0 && position > duration) {
            position = duration;
        }
        boolean ok = historyDAO.upsert(
            user.getUserId(),
            episodeId,
            position,
            duration
        );
        if (!ok) {
            write(response, 500, "error", "Không lưu được tiến trình.");
            return;
        }
        write(response, 200, "success", "OK");
    }

    private static void write(
        HttpServletResponse response,
        int http,
        String status,
        String message
    ) throws IOException {
        response.setStatus(http);
        response
            .getWriter()
            .write(
                "{\"status\":\"" +
                    status +
                    "\",\"message\":\"" +
                    message.replace("\"", "'") +
                    "\"}"
            );
    }

    private static int parseFirstValidInt(String... values) {
        if (values == null) return -1;
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                int parsed = parseInt(v);
                if (parsed >= 0) return parsed;
            }
        }
        return -1;
    }

    private static int parseInt(String s) {
        try {
            return s == null
                ? -1
                : (int) Math.round(Double.parseDouble(s.trim()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
