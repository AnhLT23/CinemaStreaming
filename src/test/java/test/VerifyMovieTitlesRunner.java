package test;

import dao.MovieDAO;
import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import model.Movie;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Standalone test runner for feature and integration checks.
 * Source: src/test/java/test/VerifyMovieTitlesRunner.java.
 */
public class VerifyMovieTitlesRunner extends DBContext {

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the main operation. */
    public static void main(String[] args) {
        System.out.println(
            "=========================================================================="
        );
        System.out.println(
            "DOI CHIEU CHINH XAC DU LIEU MOVIES.TITLE GIUA SQL SERVER VA MOVIEDAO/UI"
        );
        System.out.println(
            "=========================================================================="
        );

        VerifyMovieTitlesRunner runner = new VerifyMovieTitlesRunner();
        MovieDAO movieDAO = new MovieDAO();

        try (Connection conn = runner.getConnection()) {
            // 1. Kiểm tra 10 phim đầu tiên trực tiếp từ SQL Server
            System.out.println(
                "\n[1] 10 PHIM DAU TIEN TRONG SQL SERVER (MovieId 1 - 10):"
            );
            String sql1 =
                "SELECT MovieId, Title, OriginalTitle, ReleaseYear, Status FROM Movies WHERE MovieId <= 10 ORDER BY MovieId ASC";
            try (
                PreparedStatement ps = conn.prepareStatement(sql1);
                ResultSet rs = ps.executeQuery()
            ) {
                while (rs.next()) {
                    System.out.printf(
                        "  MovieId %2d -> SQL Title: %-35s | Year: %d | Status: %s%n",
                        rs.getInt("MovieId"),
                        rs.getString("Title"),
                        rs.getInt("ReleaseYear"),
                        rs.getString("Status")
                    );
                }
            }

            // 2. Kiểm tra các phim từ MovieId 11 đến 200 trong SQL Server
            System.out.println(
                "\n[2] MAU DU LIEU SQL SERVER CHO CAC PHIM TU MOVIEID 11 TRO DI:"
            );
            String sql2 =
                "SELECT MovieId, Title, OriginalTitle, ReleaseYear, Status FROM Movies WHERE MovieId IN (11, 20, 50, 100, 196, 200) ORDER BY MovieId ASC";
            try (
                PreparedStatement ps = conn.prepareStatement(sql2);
                ResultSet rs = ps.executeQuery()
            ) {
                while (rs.next()) {
                    System.out.printf(
                        "  MovieId %3d -> SQL Title: %-35s | Year: %d | Status: %s%n",
                        rs.getInt("MovieId"),
                        rs.getString("Title"),
                        rs.getInt("ReleaseYear"),
                        rs.getString("Status")
                    );
                }
            }

            // 3. Đối chiếu chi tiết ít nhất 5 cặp MovieId / Title giữa SQL Server và Java Model / Giao diện
            System.out.println(
                "\n[3] DOI CHIEU IT NHAT 5 CAP MOVIEID / TITLE GIUA SQL SERVER VA JAVA / GIAO DIEN:"
            );
            System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
            );
            System.out.printf(
                "%-10s | %-35s | %-35s | %-12s%n",
                "MovieId",
                "SQL Movies.Title",
                "Java Movie.getTitle()",
                "Ket Qua"
            );
            System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
            );
            int[] testMovieIds = { 1, 3, 4, 10, 196, 200 };
            for (int mid : testMovieIds) {
                String sqlTitle = null;
                String checkSql = "SELECT Title FROM Movies WHERE MovieId = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, mid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sqlTitle = rs.getString("Title");
                        }
                    }
                }

                // Lấy qua MovieDAO
                List<Movie> list = movieDAO.getMovies(
                    null,
                    null,
                    null,
                    null,
                    1,
                    200
                );
                String javaTitle = null;
                for (Movie m : list) {
                    if (m.getMovieId() == mid) {
                        javaTitle = m.getTitle();
                        break;
                    }
                }

                boolean match = sqlTitle != null && sqlTitle.equals(javaTitle);
                System.out.printf(
                    "%-10d | %-35s | %-35s | %-12s%n",
                    mid,
                    sqlTitle,
                    javaTitle,
                    match ? "KHOP 100%" : "LECH!"
                );
            }
            System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
            );

            // 5. Đối chiếu MovieDAO.searchQuick("Mai")
            System.out.println("\n[5] MOVIEDAO.SEARCHQUICK('Mai'):");
            List<Movie> searchMai = movieDAO.searchQuick("Mai");
            for (Movie m : searchMai) {
                System.out.printf(
                    "  MovieId %3d -> Java m.getTitle(): %-35s%n",
                    m.getMovieId(),
                    m.getTitle()
                );
            }

            // 6. Đối chiếu MovieDAO.searchQuick("Hanh dong")
            System.out.println("\n[6] MOVIEDAO.SEARCHQUICK('Hanh dong'):");
            List<Movie> searchHd = movieDAO.searchQuick("Hanh dong");
            for (Movie m : searchHd) {
                System.out.printf(
                    "  MovieId %3d -> Java m.getTitle(): %-35s%n",
                    m.getMovieId(),
                    m.getTitle()
                );
            }

            System.out.println(
                "\n=========================================================================="
            );
            System.out.println("HOAN TAT DOI CHIEU DU LIEU SQL VA JAVA/JSP.");
            System.out.println(
                "=========================================================================="
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
