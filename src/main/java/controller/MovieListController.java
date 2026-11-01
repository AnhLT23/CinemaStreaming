package controller;

import dao.GenreDAO;
import dao.MovieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Genre;
import model.Movie;

/**
 * Owner: Chuẩn.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/MovieListController.java.
 */
@WebServlet(name = "MovieListController", urlPatterns = { "/movies" })
public class MovieListController extends HttpServlet {

    private static final int PAGE_SIZE = 20;

    private MovieDAO movieDAO;
    private GenreDAO genreDAO;

    /** Owner: Chuẩn. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        if (this.movieDAO == null) {
            this.movieDAO = new MovieDAO();
        }
        if (this.genreDAO == null) {
            this.genreDAO = new GenreDAO();
        }
    }

    /** Owner: Chuẩn. Purpose: set movie dao. */
    public void setMovieDAO(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    /** Owner: Chuẩn. Purpose: set genre dao. */
    public void setGenreDAO(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // 1. Nhận và kiểm tra tham số page (mặc định 1, kiểm tra số nguyên hợp lệ > 0)
        int page = 1;
        String pageRaw = request.getParameter("page");
        if (pageRaw != null && !pageRaw.trim().isEmpty()) {
            try {
                int parsedPage = Integer.parseInt(pageRaw.trim());
                if (parsedPage >= 1) {
                    page = parsedPage;
                }
            } catch (NumberFormatException e) {
                // Tham số không hợp lệ (ví dụ: page=abc), giữ nguyên mặc định page = 1
                page = 1;
            }
        }

        // 2. Nhận và kiểm tra tham số genreId (hỗ trợ cả genreId hoặc fallback sang param genre)
        Integer genreId = null;
        String genreIdRaw = request.getParameter("genreId");
        if (genreIdRaw == null || genreIdRaw.trim().isEmpty()) {
            genreIdRaw = request.getParameter("genre");
        }
        if (genreIdRaw != null && !genreIdRaw.trim().isEmpty()) {
            try {
                int parsedGenreId = Integer.parseInt(genreIdRaw.trim());
                if (parsedGenreId > 0) {
                    genreId = parsedGenreId;
                }
            } catch (NumberFormatException e) {
                // Tham số thể loại không phải là số hợp lệ -> bỏ qua lọc thể loại
                genreId = null;
            }
        }

        // 3. Nhận và làm sạch tham số tìm kiếm từ khóa q
        String q = request.getParameter("q");
        if (q != null) {
            q = q.trim();
            if (q.isEmpty()) {
                q = null;
            }
        }

        // 4. Nhận các tham số lọc bổ sung nếu có (loại phim type, năm phát hành year)
        String type = request.getParameter("type");
        if (type != null) {
            type = type.trim();
            if (
                type.isEmpty() ||
                (!"SERIES".equalsIgnoreCase(type) &&
                    !"SINGLE".equalsIgnoreCase(type) &&
                    !"ANIMATION_3D".equalsIgnoreCase(type))
            ) {
                type = null;
            }
        }

        Integer year = null;
        String yearRaw = request.getParameter("year");
        if (yearRaw != null && !yearRaw.trim().isEmpty()) {
            try {
                int parsedYear = Integer.parseInt(yearRaw.trim());
                if (parsedYear >= 1900 && parsedYear <= 2030) {
                    year = parsedYear;
                }
            } catch (NumberFormatException e) {
                year = null;
            }
        }

        // 5. Lấy danh sách thể loại phục vụ dropdown bộ lọc
        List<Genre> genres = genreDAO.getAllGenres();
        if (genres == null) {
            genres = Collections.emptyList();
        }

        // 6. Lấy tổng số phim phù hợp bộ lọc và tính toán totalPages
        int totalMovies = movieDAO.countMovies(genreId, q, type, year);
        int totalPages = (int) Math.ceil((double) totalMovies / PAGE_SIZE);
        if (totalPages <= 0) {
            totalPages = 1;
        }

        // 7. Lấy danh sách phim phân trang (tối đa 20 phim/trang)
        List<Movie> movies = movieDAO.getMovies(
            genreId,
            q,
            type,
            year,
            page,
            PAGE_SIZE
        );
        if (movies == null) {
            movies = Collections.emptyList();
        }

        // 8. Tạo danh sách các năm để hiển thị trên giao diện bộ lọc
        List<Integer> years = new ArrayList<>();
        for (int y = 2026; y >= 2018; y--) {
            years.add(y);
        }

        // 9. Xác định tiêu đề hiển thị phù hợp ngữ cảnh lọc
        String pageTitle = "Khám phá phim";
        if (genreId != null) {
            for (Genre g : genres) {
                if (g.getGenreId() == genreId) {
                    pageTitle = "Phim thể loại: " + g.getGenreName();
                    break;
                }
            }
        } else if (q != null && !q.isEmpty()) {
            pageTitle = "Kết quả tìm kiếm: \"" + q + "\"";
        } else if ("SERIES".equalsIgnoreCase(type)) {
            pageTitle = "Phim bộ";
        } else if ("SINGLE".equalsIgnoreCase(type)) {
            pageTitle = "Phim lẻ";
        } else if ("ANIMATION_3D".equalsIgnoreCase(type)) {
            pageTitle = "Hoạt hình 3D";
        }

        // 10. Đưa dữ liệu vào request attributes
        request.setAttribute("movies", movies);
        request.setAttribute("totalMovies", totalMovies);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", PAGE_SIZE);
        request.setAttribute("genres", genres);
        request.setAttribute("genreId", genreId);
        request.setAttribute("selectedGenreId", genreId);
        request.setAttribute("q", q != null ? q : "");
        request.setAttribute("type", type);
        request.setAttribute("year", year);
        request.setAttribute("selectedYear", year);
        request.setAttribute("years", years);
        request.setAttribute("pageTitle", pageTitle);

        // 11. Forward tới JSP
        request
            .getRequestDispatcher("/WEB-INF/views/client/movie-list.jsp")
            .forward(request, response);
    }
}
