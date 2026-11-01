package controller.admin;

import dao.MovieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import model.Movie;
import ultil.SecurityUtil;

/**
 * Owner: Hữu Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminMovieController.java.
 */
@WebServlet("/admin/movies")
public class AdminMovieController extends HttpServlet {

    private final MovieDAO dao = new MovieDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            SecurityUtil.csrf(req);
            String action = req.getParameter("action");
            if (action == null) {
                action = "list";
            }
            if ("create".equals(action)) {
                Movie movie = new Movie();
                movie.setMovieType("SERIES");
                movie.setCurrentEpisode(1);
                movie.setTotalEpisodes(1);
                req.setAttribute("movie", movie);
                req.getRequestDispatcher(
                    "/WEB-INF/views/admin/movie-form.jsp"
                ).forward(req, resp);
                return;
            }
            if ("edit".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                if (id <= 0) {
                    resp.sendError(400, "Movie ID không hợp lệ.");
                    return;
                }
                Movie m = dao.getById(id);
                if (m == null || "DELETED".equals(m.getStatus())) {
                    resp.sendError(404);
                    return;
                }
                req.setAttribute("movie", m);
                req.getRequestDispatcher(
                    "/WEB-INF/views/admin/movie-form.jsp"
                ).forward(req, resp);
                return;
            }
            req.setAttribute(
                "movies",
                dao.getAll(req.getParameter("q"), req.getParameter("status"))
            );
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/movie-manage.jsp"
            ).forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(400, "Movie ID không hợp lệ.");
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
        String action = req.getParameter("action");
        if ("delete".equals(action)) {
            try {
                int movieId = parsePositiveInt(
                    req.getParameter("movieId"),
                    "Movie ID không hợp lệ."
                );
                if (!dao.softDelete(movieId)) {
                    resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy phim cần xóa."
                    );
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/admin/movies");
            } catch (IllegalArgumentException ex) {
                resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage()
                );
            }
            return;
        }
        if (!"save".equals(action)) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Action không hợp lệ."
            );
            return;
        }

        Movie m = new Movie();
        try {
            String id = req.getParameter("movieId");
            if (id != null && !id.trim().isEmpty()) {
                m.setMovieId(parsePositiveInt(id, "Movie ID không hợp lệ."));
            }
            m.setTitle(trim(req.getParameter("title")));
            m.setOriginalTitle(req.getParameter("originalTitle"));
            m.setDescription(req.getParameter("description"));
            m.setCountry(req.getParameter("country"));
            m.setDirector(req.getParameter("director"));
            m.setActors(req.getParameter("actors"));
            m.setPosterUrl(req.getParameter("posterUrl"));
            m.setBackdropUrl(req.getParameter("backdropUrl"));
            m.setTrailerUrl(req.getParameter("trailerUrl"));
            m.setMovieType(req.getParameter("movieType"));
            m.setAgeRating(req.getParameter("ageRating"));
            m.setAudioSummary(req.getParameter("audioSummary"));
            m.setStatus("PUBLISHED");
            m.setVipOnly(req.getParameter("isVipOnly") != null);
            m.setRentOnly(req.getParameter("isRentOnly") != null);
            m.setDuration(
                parsePositiveInt(
                    req.getParameter("duration"),
                    "Thời lượng phải là số nguyên dương."
                )
            );
            m.setReleaseYear(
                parseOptionalInt(
                    req.getParameter("releaseYear"),
                    "Năm phát hành phải là số nguyên."
                )
            );
            m.setCurrentEpisode(
                parseOptionalPositiveInt(
                    req.getParameter("currentEpisode"),
                    1,
                    "Tập hiện tại phải là số nguyên dương."
                )
            );
            m.setTotalEpisodes(
                parseOptionalPositiveInt(
                    req.getParameter("totalEpisodes"),
                    1,
                    "Tổng số tập phải là số nguyên dương."
                )
            );
            String rentalPrice = req.getParameter("rentalPrice");
            m.setRentalPrice(
                new BigDecimal(
                    rentalPrice == null || rentalPrice.trim().isEmpty()
                        ? "0"
                        : rentalPrice.trim()
                )
            );
            String error = validate(m);
            if (error != null) {
                req.setAttribute("error", error);
                showForm(req, resp, m);
                return;
            }
            if (m.getMovieId() > 0) {
                dao.update(m);
            } else {
                dao.insert(m);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/movies");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showForm(req, resp, m);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String validate(Movie m) {
        if (
            m.getTitle() == null ||
            m.getTitle().isBlank() ||
            m.getTitle().length() > 200
        ) {
            return "Tên phim phải có từ 1 đến 200 ký tự.";
        }
        if (m.getDuration() <= 0 || m.getDuration() > 1000) {
            return "Thời lượng phải từ 1 đến 1000 phút.";
        }
        if (
            m.getReleaseYear() != null &&
            (m.getReleaseYear() < 1888 || m.getReleaseYear() > 2100)
        ) {
            return "Năm phát hành không hợp lệ.";
        }
        if (
            !"SERIES".equals(m.getMovieType()) &&
            !"SINGLE".equals(m.getMovieType()) &&
            !"ANIMATION_3D".equals(m.getMovieType())
        ) {
            return "Loại phim không hợp lệ.";
        }
        if (
            m.getTotalEpisodes() < 1 ||
            m.getCurrentEpisode() < 1 ||
            m.getCurrentEpisode() > m.getTotalEpisodes()
        ) {
            return "Thông tin số tập không hợp lệ.";
        }
        if (
            m.getRentalPrice().signum() < 0 ||
            m.getRentalPrice().scale() > 2 ||
            m.getRentalPrice().precision() > 18
        ) {
            return "Giá thuê phải là số tiền không âm, tối đa 2 chữ số thập phân.";
        }
        return null;
    }

    private void showForm(
        HttpServletRequest req,
        HttpServletResponse resp,
        Movie movie
    ) throws ServletException, IOException {
        SecurityUtil.csrf(req);
        req.setAttribute("movie", movie);
        req.getRequestDispatcher("/WEB-INF/views/admin/movie-form.jsp").forward(
            req,
            resp
        );
    }

    private int parsePositiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(message);
    }

    private Integer parseOptionalInt(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parseOptionalPositiveInt(
        String value,
        int defaultValue,
        String message
    ) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return parsePositiveInt(value, message);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
