package controller;

import dao.BannerDAO;
import dao.MovieDAO;
import dao.WatchHistoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import model.Banner;
import model.Movie;
import model.User;
import model.WatchHistory;

/**
 * Owner: Chuẩn.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/HomeController.java.
 */
@WebServlet(name = "HomeController", urlPatterns = { "/home", "/trang-chu" })
public class HomeController extends HttpServlet {

    private BannerDAO bannerDAO;
    private MovieDAO movieDAO;
    private WatchHistoryDAO watchHistoryDAO;

    /** Owner: Chuẩn. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        this.bannerDAO = new BannerDAO();
        this.movieDAO = new MovieDAO();
        this.watchHistoryDAO = new WatchHistoryDAO();
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        // 1. Lấy danh sách banner HOME_HERO đang hoạt động
        List<Banner> banners = bannerDAO.getActiveHeroBanners();
        if (banners == null) {
            banners = Collections.emptyList();
        }

        // 2. Lấy danh sách phim thịnh hành, phim bộ mới nhất và phim lẻ mới nhất
        List<Movie> trendingMovies = movieDAO.getTrendingMovies(10);
        if (trendingMovies == null) {
            trendingMovies = Collections.emptyList();
        }

        List<Movie> latestSeries = movieDAO.getLatestSeries(10);
        if (latestSeries == null) {
            latestSeries = Collections.emptyList();
        }

        List<Movie> latestSingles = movieDAO.getLatestSingles(10);
        if (latestSingles == null) {
            latestSingles = Collections.emptyList();
        }

        // 3. Kiểm tra người dùng đăng nhập trong Session ("loggedInUser" hoặc fallback "user")
        HttpSession session = request.getSession(false);
        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                loggedInUser = (User) session.getAttribute("user");
            }
        }

        // 4. Lấy lịch sử xem dở chưa hoàn thành nếu đã đăng nhập
        List<WatchHistory> continueWatching = Collections.emptyList();
        if (loggedInUser != null) {
            continueWatching = watchHistoryDAO.getContinueWatchingByUserId(
                loggedInUser.getUserId(),
                10
            );
            if (continueWatching == null) {
                continueWatching = Collections.emptyList();
            }
        }

        // 5. Đưa dữ liệu vào request attributes khớp với các biểu thức EL trên JSP
        request.setAttribute("banners", banners);
        request.setAttribute("trendingMovies", trendingMovies);
        request.setAttribute("latestSeries", latestSeries);
        request.setAttribute("latestSingles", latestSingles);
        request.setAttribute("seriesMovies", latestSeries);
        request.setAttribute("singleMovies", latestSingles);
        request.setAttribute("continueWatching", continueWatching);

        // 6. Forward tới home.jsp
        request
            .getRequestDispatcher("/WEB-INF/views/client/home.jsp")
            .forward(request, response);
    }
}
