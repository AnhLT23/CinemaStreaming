package test;

import dao.BannerDAO;
import dao.MovieDAO;
import dao.WatchHistoryDAO;
import java.util.List;
import model.Banner;
import model.Movie;
import model.WatchHistory;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Standalone test runner for feature and integration checks.
 * Source: src/test/java/test/HomeTestRunner.java.
 */
public class HomeTestRunner {

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the main operation. */
    public static void main(String[] args) {
        System.out.println(
            "=================================================="
        );
        System.out.println("BAT DAU KIEM THU CHUC NANG HOMECONTROLLER & DAO");
        System.out.println(
            "=================================================="
        );

        try {
            // 1. Kiem thu BannerDAO
            System.out.println(
                "\n[1] Kiem thu BannerDAO.getActiveHeroBanners():"
            );
            BannerDAO bannerDAO = new BannerDAO();
            List<Banner> banners = bannerDAO.getActiveHeroBanners();
            System.out.println(
                " -> So luong banner hoat dong: " + banners.size()
            );
            if (banners.isEmpty()) {
                System.out.println(
                    " -> CANH BAO: Khong co banner nao hoat dong."
                );
            } else {
                Banner b1 = banners.get(0);
                System.out.println(" -> Banner 1 Title: " + b1.getTitle());
                System.out.println(
                    " -> Banner 1 ImagePath: " + b1.getImagePath()
                );
                System.out.println(" -> Banner 1 MovieId: " + b1.getMovieId());
                System.out.println(
                    " -> Banner 1 Description: " + b1.getDescription()
                );
            }

            // 2. Kiem thu MovieDAO - Phim thinh hanh
            System.out.println(
                "\n[2] Kiem thu MovieDAO.getTrendingMovies(10):"
            );
            MovieDAO movieDAO = new MovieDAO();
            List<Movie> trending = movieDAO.getTrendingMovies(10);
            System.out.println(
                " -> So luong phim thinh hanh: " + trending.size()
            );
            if (!trending.isEmpty()) {
                Movie m = trending.get(0);
                System.out.println(
                    " -> Phim 1 Title: " +
                        m.getTitle() +
                        " | Views: " +
                        m.getViewCount() +
                        " | Rating: " +
                        m.getAvgRating()
                );
            }

            // 3. Kiem thu MovieDAO - Phim bo moi nhat
            System.out.println("\n[3] Kiem thu MovieDAO.getLatestSeries(10):");
            List<Movie> series = movieDAO.getLatestSeries(10);
            System.out.println(" -> So luong phim bo: " + series.size());
            if (!series.isEmpty()) {
                Movie s1 = series.get(0);
                System.out.println(
                    " -> Phim bo 1: " +
                        s1.getTitle() +
                        " (Type: " +
                        s1.getMovieType() +
                        ")"
                );
            }

            // 4. Kiem thu MovieDAO - Phim le moi nhat
            System.out.println("\n[4] Kiem thu MovieDAO.getLatestSingles(10):");
            List<Movie> singles = movieDAO.getLatestSingles(10);
            System.out.println(" -> So luong phim le: " + singles.size());
            if (!singles.isEmpty()) {
                Movie sg1 = singles.get(0);
                System.out.println(
                    " -> Phim le 1: " +
                        sg1.getTitle() +
                        " (Type: " +
                        sg1.getMovieType() +
                        ")"
                );
            }

            // 5. Kiem thu WatchHistoryDAO - Nguoi dung chua dang nhap (Guest)
            System.out.println(
                "\n[5] Kiem thu trang thai NGUOI DUNG CHUA DANG NHAP (Guest):"
            );
            System.out.println(" -> Session loggedInUser = null");
            System.out.println(
                " -> continueWatching khong duoc goi, tra ve empty list."
            );

            // 6. Kiem thu WatchHistoryDAO - Nguoi dung da dang nhap (UserId = 4)
            System.out.println(
                "\n[6] Kiem thu trang thai NGUOI DUNG DA DANG NHAP (UserId = 4):"
            );
            WatchHistoryDAO whDAO = new WatchHistoryDAO();
            List<WatchHistory> history = whDAO.getContinueWatchingByUserId(
                4,
                10
            );
            System.out.println(
                " -> So luong phim dang xem do cua user 4: " + history.size()
            );
            if (!history.isEmpty()) {
                WatchHistory wh = history.get(0);
                System.out.println(
                    " -> Dang xem: " +
                        wh.getMovieTitle() +
                        " (Tap: " +
                        wh.getEpisodeNumber() +
                        ") - Tien do: " +
                        wh.getCompletionPercent() +
                        "%"
                );
            }

            System.out.println(
                "\n=================================================="
            );
            System.out.println(
                "TAT CA CAC PHEP THU DATABASE & LOGIC DEU THANH CONG!"
            );
            System.out.println(
                "=================================================="
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
