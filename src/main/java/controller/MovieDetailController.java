package controller;

import dao.CommentDAO;
import dao.EpisodeDAO;
import dao.MovieDAO;
import dao.MovieRentalDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import model.Comment;
import model.Movie;
import model.MovieEpisode;
import model.MovieRental;
import model.User;
import ultil.PlaybackAccess;

/**
 * Owner: Tuấn Anh.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/MovieDetailController.java.
 */
@WebServlet(
    name = "MovieDetailController",
    urlPatterns = { "/movie", "/chi-tiet-phim" }
)
public class MovieDetailController extends HttpServlet {

    private MovieDAO movieDAO;
    private EpisodeDAO episodeDAO;
    private MovieRentalDAO rentalDAO;
    private CommentDAO commentDAO;

    /** Owner: Tuấn Anh. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        movieDAO = new MovieDAO();
        episodeDAO = new EpisodeDAO();
        rentalDAO = new MovieRentalDAO();
        commentDAO = new CommentDAO();
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // 1. Đọc id (chấp nhận cả id và movieId), sai định dạng -> 404
        String raw = request.getParameter("id");
        if (raw == null || raw.isBlank()) {
            raw = request.getParameter("movieId");
        }
        int movieId;
        try {
            movieId = Integer.parseInt(raw == null ? "" : raw.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Movie movie = movieDAO.getById(movieId);
        if (movie == null || !"PUBLISHED".equalsIgnoreCase(movie.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 2. Nạp danh sách tập và bình luận
        List<MovieEpisode> episodes = episodeDAO.getEpisodesByMovieId(movieId);
        List<Comment> comments = commentDAO.getCommentsByMovieId(movieId);
        if (comments == null) {
            comments = Collections.emptyList();
        }

        // 3. Quyết định nút "Xem phim" hay "Thuê vé 48H" dựa trên vé thuê của user
        HttpSession session = request.getSession(false);
        User user = PlaybackAccess.currentUser(session);
        boolean canWatch = true;
        MovieRental activeRental = null;
        if (movie.isRentOnly()) {
            canWatch = PlaybackAccess.isAdmin(session);
            if (user != null) {
                activeRental = rentalDAO.getActiveRental(
                    user.getUserId(),
                    movieId
                );
                canWatch = canWatch || activeRental != null;
            }
        } else if (movie.isVipOnly()) {
            canWatch =
                PlaybackAccess.isAdmin(session) ||
                PlaybackAccess.isVipActive(user);
        }

        // Chuẩn hóa link Trailer YouTube sang dạng Embed để có thể phát trực tiếp trong Modal
        String trailerEmbedUrl = null;
        if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().isBlank()) {
            trailerEmbedUrl = EpisodeDAO.normalizePlayableUrl(
                movie.getTrailerUrl(),
                "IFRAME_EMBED"
            );
        }

        // 4. Điểm đánh giá hiển thị (thang 10) & các thuộc tính cho movie-detail.jsp
        request.setAttribute("movie", movie);
        request.setAttribute("episodes", episodes);
        request.setAttribute("comments", comments);
        request.setAttribute(
            "averageRating",
            movie.getTotalReviews() > 0
                ? String.format(
                      java.util.Locale.US,
                      "%.1f",
                      movie.getAvgRating()
                  )
                : null
        );
        request.setAttribute("ratingCount", movie.getTotalReviews());
        request.setAttribute("canWatch", canWatch);
        request.setAttribute("isRentalValid", activeRental != null || canWatch);
        request.setAttribute("activeRental", activeRental);
        request.setAttribute(
            "firstEpisode",
            episodes.isEmpty() ? null : episodes.get(0)
        );
        request.setAttribute("trailerEmbedUrl", trailerEmbedUrl);

        request
            .getRequestDispatcher("/WEB-INF/views/client/movie-detail.jsp")
            .forward(request, response);
    }
}
