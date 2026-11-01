package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Movie;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/MovieDAO.java.
 */
public class MovieDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        MovieDAO.class.getName()
    );

    private static final String SELECT_BASE =
        "SELECT MovieId, Title, OriginalTitle, Description, Duration, ReleaseYear, " +
        "       Country, Director, Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, " +
        "       AgeRating, AudioSummary, " +
        "       CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, " +
        "       IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt, UpdatedAt " +
        "FROM Movies ";

    /**
     * Lấy danh sách phim thịnh hành (nhiều lượt xem và đánh giá cao nhất). Chỉ
     * lấy phim có Status = 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getTrendingMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) MovieId, Title, OriginalTitle, Description, Duration, ReleaseYear, " +
            "       Country, Director, Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, " +
            "       CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, " +
            "       IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt, UpdatedAt " +
            "FROM Movies " +
            "WHERE Status = 'PUBLISHED' " +
            "ORDER BY ViewCount DESC, AvgRating DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách phim thịnh hành",
                e
            );
        }
        return list;
    }

    /**
     * Lấy danh sách phim bộ mới nhất (MovieType = 'SERIES'). Chỉ lấy phim có
     * Status = 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getLatestSeries(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) MovieId, Title, OriginalTitle, Description, Duration, ReleaseYear, " +
            "       Country, Director, Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, " +
            "       CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, " +
            "       IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt, UpdatedAt " +
            "FROM Movies " +
            "WHERE Status = 'PUBLISHED' AND MovieType = 'SERIES' " +
            "ORDER BY CreatedAt DESC, MovieId DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách phim bộ mới nhất",
                e
            );
        }
        return list;
    }

    /**
     * Lấy danh sách phim lẻ mới nhất (MovieType = 'SINGLE'). Chỉ lấy phim có
     * Status = 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getLatestSingles(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) MovieId, Title, OriginalTitle, Description, Duration, ReleaseYear, " +
            "       Country, Director, Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, " +
            "       CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, " +
            "       IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt, UpdatedAt " +
            "FROM Movies " +
            "WHERE Status = 'PUBLISHED' AND MovieType = 'SINGLE' " +
            "ORDER BY CreatedAt DESC, MovieId DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách phim lẻ mới nhất",
                e
            );
        }
        return list;
    }

    /**
     * Lấy danh sách phim mới phát hành nói chung. Chỉ lấy phim có Status =
     * 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getLatestMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) MovieId, Title, OriginalTitle, Description, Duration, ReleaseYear, " +
            "       Country, Director, Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, " +
            "       CurrentEpisode, TotalEpisodes, ViewCount, AvgRating, TotalReviews, " +
            "       IsVipOnly, IsRentOnly, RentalPrice, Status, CreatedAt, UpdatedAt " +
            "FROM Movies " +
            "WHERE Status = 'PUBLISHED' " +
            "ORDER BY CreatedAt DESC, MovieId DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, limit <= 0 ? 10 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách phim mới nhất", e);
        }
        return list;
    }

    /**
     * Đếm tổng số phim phù hợp bộ lọc để tính totalPages. Chỉ lấy phim có
     * Status = 'PUBLISHED'.
     */
    /**
     * Đếm tổng số phim phù hợp bộ lọc để tính totalPages. Hỗ trợ tìm kiếm thông
     * minh đa trường (Title, OriginalTitle, Genre, ReleaseYear, Actors,
     * Director, Country, Description, MovieType). Chỉ lấy phim có Status =
     * 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */

    public int countMovies(
        Integer genreId,
        String q,
        String type,
        Integer year
    ) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM Movies m WHERE m.Status = 'PUBLISHED' "
        );
        List<Object> params = new ArrayList<>();

        buildFilterConditions(sql, params, genreId, q, type, year);

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đếm tổng số phim", e);
        }
        return 0;
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: count movies. */
    public int countMovies(Integer genreId, String q) {
        return countMovies(genreId, q, null, null);
    }

    /**
     * Lấy danh sách phim có lọc và phân trang bằng OFFSET / FETCH NEXT trong
     * SQL Server. Khi có từ khóa tìm kiếm q, sắp xếp theo mức độ ưu tiên khớp:
     * 1. Khớp Title / OriginalTitle 2. Khớp Thể loại (Genres) 3. Khớp Năm phát
     * hành (ReleaseYear) 4. Khớp Diễn viên (Actors) 5. Khớp Đạo diễn (Director)
     * 6. Khớp Quốc gia (Country) 7. Khớp Mô tả phim (Description) Sau đó ưu
     * tiên lượt xem cao nhất (ViewCount DESC) và MovieId DESC. Chỉ lấy phim có
     * Status = 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getMovies(
        Integer genreId,
        String q,
        String type,
        Integer year,
        int page,
        int pageSize
    ) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT m.MovieId, m.Title, m.OriginalTitle, m.Description, m.Duration, m.ReleaseYear, " +
                "       m.Country, m.Director, m.Actors, m.PosterUrl, m.BackdropUrl, m.TrailerUrl, m.MovieType, " +
                "       m.CurrentEpisode, m.TotalEpisodes, m.ViewCount, m.AvgRating, m.TotalReviews, " +
                "       m.IsVipOnly, m.IsRentOnly, m.RentalPrice, m.Status, m.CreatedAt, m.UpdatedAt " +
                "FROM Movies m " +
                "WHERE m.Status = 'PUBLISHED' "
        );
        List<Object> params = new ArrayList<>();

        buildFilterConditions(sql, params, genreId, q, type, year);

        // Áp dụng sắp xếp thông minh theo độ ưu tiên khi có từ khóa q
        if (q != null && !q.trim().isEmpty()) {
            appendSmartOrderByClause(sql, params, q.trim());
        } else {
            sql.append(" ORDER BY m.MovieId DESC ");
        }

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        int safePage = Math.max(1, page);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;
        int offset = (safePage - 1) * safePageSize;

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            int idx = 1;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, safePageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách phim phân trang",
                e
            );
        }
        return list;
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve movies. */
    public List<Movie> getMovies(
        Integer genreId,
        String q,
        int page,
        int pageSize
    ) {
        return getMovies(genreId, q, null, null, page, pageSize);
    }

    /**
     * Tìm kiếm nhanh (Live Search) tối đa 5 phim. Hỗ trợ tìm kiếm thông minh đa
     * trường: Tên, Tên gốc, Thể loại, Năm, Diễn viên, Đạo diễn, Quốc gia, Mô
     * tả, Loại phim. Nhận diện truy vấn tự nhiên (ví dụ: "phim hành động
     * 2024"). Ưu tiên khớp tên phim trước, tiếp đến thể loại, năm, diễn viên,
     * đạo diễn, quốc gia và mô tả. Chỉ lấy phim có Status = 'PUBLISHED'.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */

    public List<Movie> searchQuick(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String trimmed = q.trim();
        if (trimmed.length() > 100) {
            trimmed = trimmed.substring(0, 100).trim();
        }

        ultil.SearchQueryParser.ParsedQuery parsed =
            ultil.SearchQueryParser.parse(trimmed);

        // 1. Nếu là truy vấn phức hợp có cấu trúc rõ ràng (ví dụ: "phim hành động 2024")
        if (parsed != null && parsed.isCompound()) {
            List<Movie> compoundResults = executeCompoundSearchQuick(parsed);
            if (!compoundResults.isEmpty()) {
                return compoundResults;
            }
        }

        // 2. Tìm kiếm thông minh đa trường với sắp xếp ưu tiên
        return executeSmartSearchQuick(trimmed);
    }

    /**
     * Thực hiện tìm kiếm nhanh cho truy vấn phức hợp đã xác định các tiêu chí
     * (năm, thể loại, loại phim, từ khóa).
     */
    private List<Movie> executeCompoundSearchQuick(
        ultil.SearchQueryParser.ParsedQuery parsed
    ) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP 5 m.MovieId, m.Title, m.OriginalTitle, m.Description, m.Duration, m.ReleaseYear, " +
                "       m.Country, m.Director, m.Actors, m.PosterUrl, m.BackdropUrl, m.TrailerUrl, m.MovieType, " +
                "       m.CurrentEpisode, m.TotalEpisodes, m.ViewCount, m.AvgRating, m.TotalReviews, " +
                "       m.IsVipOnly, m.IsRentOnly, m.RentalPrice, m.Status, m.CreatedAt, m.UpdatedAt " +
                "FROM Movies m " +
                "WHERE m.Status = 'PUBLISHED' "
        );
        List<Object> params = new ArrayList<>();

        if (parsed.getDetectedGenreId() != null) {
            sql.append(
                " AND EXISTS (SELECT 1 FROM MovieGenres mg WHERE mg.MovieId = m.MovieId AND mg.GenreId = ?) "
            );
            params.add(parsed.getDetectedGenreId());
        }
        if (parsed.getDetectedYear() != null) {
            sql.append(" AND m.ReleaseYear = ? ");
            params.add(parsed.getDetectedYear());
        }
        if (parsed.getDetectedMovieType() != null) {
            sql.append(" AND m.MovieType = ? ");
            params.add(parsed.getDetectedMovieType());
        }
        if (parsed.getDetectedCountry() != null) {
            sql.append(
                " AND m.Country COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            );
            params.add(
                "%" +
                    ultil.SearchQueryParser.escapeLike(
                        parsed.getDetectedCountry()
                    ) +
                    "%"
            );
        }
        if (parsed.getKeyword() != null && !parsed.getKeyword().isEmpty()) {
            appendSmartKeywordClause(sql, params, parsed.getKeyword());
        }

        sql.append(" ORDER BY m.ViewCount DESC, m.MovieId DESC ");

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm nhanh phim phức hợp", e);
        }
        return list;
    }

    /**
     * Thực hiện tìm kiếm nhanh thông minh trên toàn bộ các trường dữ liệu với
     * xếp hạng ưu tiên.
     */
    private List<Movie> executeSmartSearchQuick(String q) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP 5 m.MovieId, m.Title, m.OriginalTitle, m.Description, m.Duration, m.ReleaseYear, " +
                "       m.Country, m.Director, m.Actors, m.PosterUrl, m.BackdropUrl, m.TrailerUrl, m.MovieType, " +
                "       m.CurrentEpisode, m.TotalEpisodes, m.ViewCount, m.AvgRating, m.TotalReviews, " +
                "       m.IsVipOnly, m.IsRentOnly, m.RentalPrice, m.Status, m.CreatedAt, m.UpdatedAt " +
                "FROM Movies m " +
                "WHERE m.Status = 'PUBLISHED' "
        );
        List<Object> params = new ArrayList<>();

        appendSmartKeywordClause(sql, params, q);
        appendSmartOrderByClause(sql, params, q);

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMovie(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi tìm kiếm nhanh phim thông minh",
                e
            );
        }
        return list;
    }

    /**
     * Xây dựng điều kiện lọc chung cho countMovies và getMovies đảm bảo tính
     * nhất quán 100%.
     */
    private void buildFilterConditions(
        StringBuilder sql,
        List<Object> params,
        Integer genreId,
        String q,
        String type,
        Integer year
    ) {
        // 1. Lọc thể loại rõ ràng qua dropdown
        if (genreId != null && genreId > 0) {
            sql.append(
                " AND EXISTS (SELECT 1 FROM MovieGenres mg WHERE mg.MovieId = m.MovieId AND mg.GenreId = ?) "
            );
            params.add(genreId);
        }

        // 2. Lọc loại phim qua dropdown/tab
        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND m.MovieType = ? ");
            params.add(type.trim());
        }

        // 3. Lọc năm phát hành qua dropdown
        if (year != null && year > 0) {
            sql.append(" AND m.ReleaseYear = ? ");
            params.add(year);
        }

        // 4. Lọc thông minh theo từ khóa q
        if (q != null && !q.trim().isEmpty()) {
            String trimmedQ = q.trim();
            if (trimmedQ.length() > 100) {
                trimmedQ = trimmedQ.substring(0, 100).trim();
            }

            ultil.SearchQueryParser.ParsedQuery parsed =
                ultil.SearchQueryParser.parse(trimmedQ);

            if (parsed != null && parsed.isCompound()) {
                // Nhận diện thể loại tự nhiên nếu dropdown chưa chọn
                if (genreId == null && parsed.getDetectedGenreId() != null) {
                    sql.append(
                        " AND EXISTS (SELECT 1 FROM MovieGenres mg WHERE mg.MovieId = m.MovieId AND mg.GenreId = ?) "
                    );
                    params.add(parsed.getDetectedGenreId());
                }
                // Nhận diện năm tự nhiên nếu dropdown chưa chọn
                if (year == null && parsed.getDetectedYear() != null) {
                    sql.append(" AND m.ReleaseYear = ? ");
                    params.add(parsed.getDetectedYear());
                }
                // Nhận diện loại phim tự nhiên nếu filter chưa chọn
                if (type == null && parsed.getDetectedMovieType() != null) {
                    sql.append(" AND m.MovieType = ? ");
                    params.add(parsed.getDetectedMovieType());
                }
                // Nhận diện quốc gia tự nhiên
                if (parsed.getDetectedCountry() != null) {
                    sql.append(
                        " AND m.Country COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
                    );
                    params.add(
                        "%" +
                            ultil.SearchQueryParser.escapeLike(
                                parsed.getDetectedCountry()
                            ) +
                            "%"
                    );
                }
                // Nếu còn từ khóa tự do đi kèm
                if (
                    parsed.getKeyword() != null &&
                    !parsed.getKeyword().isEmpty()
                ) {
                    appendSmartKeywordClause(sql, params, parsed.getKeyword());
                }
            } else {
                // Truy vấn từ khóa tự do: tìm kiếm trên toàn bộ các trường dữ liệu
                appendSmartKeywordClause(sql, params, trimmedQ);
            }
        }
    }

    /**
     * Thêm mệnh đề so khớp đa trường thông minh cho từ khóa keyword. Sử dụng
     * collation SQL_Latin1_General_CP1_CI_AI để hỗ trợ tiếng Việt có dấu và
     * không dấu hoàn toàn. Sử dụng EXISTS để tránh trả kết quả trùng lặp khi
     * một phim có nhiều thể loại.
     */
    private void appendSmartKeywordClause(
        StringBuilder sql,
        List<Object> params,
        String keyword
    ) {
        String escaped = ultil.SearchQueryParser.escapeLike(keyword);
        String pattern = "%" + escaped + "%";

        sql.append(" AND (")
            .append(
                "    m.Title COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(
                " OR m.OriginalTitle COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(
                " OR m.Actors COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(
                " OR m.Director COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(
                " OR m.Country COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(
                " OR m.Description COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' "
            )
            .append(" OR CAST(m.ReleaseYear AS VARCHAR(10)) LIKE ? ESCAPE '!' ")
            .append(
                " OR (m.MovieType = 'SERIES' AND (? LIKE '%bo%' OR ? LIKE '%series%')) "
            )
            .append(
                " OR (m.MovieType = 'SINGLE' AND (? LIKE '%le%' OR ? LIKE '%single%')) "
            )
            .append(
                " OR EXISTS (SELECT 1 FROM MovieGenres mg JOIN Genres g ON mg.GenreId = g.GenreId "
            )
            .append(
                "            WHERE mg.MovieId = m.MovieId AND g.GenreName COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!') "
            )
            .append(" ) ");

        params.add(pattern); // Title
        params.add(pattern); // OriginalTitle
        params.add(pattern); // Actors
        params.add(pattern); // Director
        params.add(pattern); // Country
        params.add(pattern); // Description
        params.add(pattern); // ReleaseYear
        params.add(keyword); // SERIES token
        params.add(keyword); // SERIES token
        params.add(keyword); // SINGLE token
        params.add(keyword); // SINGLE token
        params.add(pattern); // GenreName
    }

    /**
     * Thêm mệnh đề ORDER BY ưu tiên xếp hạng độ liên quan: 1. Tiêu đề (Title /
     * OriginalTitle) 2. Thể loại (Genres) 3. Năm phát hành (ReleaseYear) 4.
     * Diễn viên (Actors) 5. Đạo diễn (Director) 6. Quốc gia (Country) 7. Mô tả
     * phim (Description) Kế tiếp là ViewCount DESC và MovieId DESC.
     */
    private void appendSmartOrderByClause(
        StringBuilder sql,
        List<Object> params,
        String q
    ) {
        String escaped = ultil.SearchQueryParser.escapeLike(q);
        String pattern = "%" + escaped + "%";

        sql.append(" ORDER BY CASE ")
            .append(
                "   WHEN (m.Title COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' OR m.OriginalTitle COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!') THEN 1 "
            )
            .append(
                "   WHEN EXISTS (SELECT 1 FROM MovieGenres mg JOIN Genres g ON mg.GenreId = g.GenreId WHERE mg.MovieId = m.MovieId AND g.GenreName COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!') THEN 2 "
            )
            .append(
                "   WHEN CAST(m.ReleaseYear AS VARCHAR(10)) LIKE ? ESCAPE '!' THEN 3 "
            )
            .append(
                "   WHEN m.Actors COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' THEN 4 "
            )
            .append(
                "   WHEN m.Director COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' THEN 5 "
            )
            .append(
                "   WHEN m.Country COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' THEN 6 "
            )
            .append(
                "   WHEN m.Description COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? ESCAPE '!' THEN 7 "
            )
            .append("   ELSE 8 ")
            .append(" END ASC, m.ViewCount DESC, m.MovieId DESC ");

        params.add(pattern); // Title
        params.add(pattern); // OriginalTitle
        params.add(pattern); // GenreName
        params.add(pattern); // ReleaseYear
        params.add(pattern); // Actors
        params.add(pattern); // Director
        params.add(pattern); // Country
        params.add(pattern); // Description
    }

    /**
     * Map một hàng ResultSet sang Movie Model đối tượng.
     */
    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("MovieId"));
        m.setTitle(rs.getString("Title"));
        m.setOriginalTitle(rs.getString("OriginalTitle"));
        m.setDescription(rs.getString("Description"));
        m.setDuration(rs.getInt("Duration"));
        int releaseYear = rs.getInt("ReleaseYear");
        m.setReleaseYear(rs.wasNull() ? null : releaseYear);
        m.setCountry(rs.getString("Country"));
        m.setDirector(rs.getString("Director"));
        m.setActors(rs.getString("Actors"));
        m.setPosterUrl(rs.getString("PosterUrl"));
        m.setBackdropUrl(rs.getString("BackdropUrl"));
        m.setTrailerUrl(rs.getString("TrailerUrl"));
        m.setMovieType(rs.getString("MovieType"));
        m.setCurrentEpisode(rs.getInt("CurrentEpisode"));
        m.setTotalEpisodes(rs.getInt("TotalEpisodes"));
        m.setViewCount(rs.getLong("ViewCount"));
        m.setAvgRating(rs.getDouble("AvgRating"));
        m.setTotalReviews(rs.getInt("TotalReviews"));
        m.setVipOnly(rs.getBoolean("IsVipOnly"));
        m.setRentOnly(rs.getBoolean("IsRentOnly"));
        m.setRentalPrice(rs.getBigDecimal("RentalPrice"));
        m.setStatus(rs.getString("Status"));
        m.setCreatedAt(rs.getTimestamp("CreatedAt"));
        m.setUpdatedAt(rs.getTimestamp("UpdatedAt"));

        // Thuộc tính mở rộng hiển thị trên giao diện
        m.setQuality(m.isVipOnly() ? "4K" : "1080P");
        m.setAudio("Vietsub");
        return m;
    }

    /**
     * Lấy chi tiết một phim theo id (bỏ qua phim đã xóa mềm).
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public Movie getById(int movieId) {
        String sql = SELECT_BASE + "WHERE MovieId = ? AND Status <> 'DELETED'";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Movie m = mapResultSetToMovie(rs);
                    m.setAgeRating(rs.getString("AgeRating"));
                    m.setAudioSummary(rs.getString("AudioSummary"));
                    m.setGenres(getGenreNames(conn, movieId));
                    return m;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy phim theo id: " + movieId, e);
        }
        return null;
    }

    private String getGenreNames(Connection conn, int movieId)
        throws SQLException {
        String sql =
            "SELECT g.GenreName FROM MovieGenres mg JOIN Genres g ON g.GenreId = mg.GenreId " +
            "WHERE mg.MovieId = ? ORDER BY g.GenreName";
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(rs.getString(1));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Tăng lượt xem của phim thêm 1.
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public void incrementViewCount(int movieId) {
        String sql =
            "UPDATE Movies SET ViewCount = ViewCount + 1 WHERE MovieId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi tăng lượt xem phim: " + movieId,
                e
            );
        }
    }

    /**
     * Lấy danh sách phim phân trang tùy chỉnh kích thước (dùng cho Phim Cùng
     * Thể Loại ở WatchController).
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public List<Movie> getMovieList(int page, int pageSize, Object genreIdObj) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 4;
        }
        Integer genreId = null;
        if (genreIdObj instanceof Integer) {
            genreId = (Integer) genreIdObj;
        } else if (genreIdObj != null) {
            try {
                genreId = Integer.parseInt(genreIdObj.toString().trim());
            } catch (NumberFormatException ignored) {}
        }

        // Gọi trực tiếp phương thức getMovies có sẵn trong MovieDAO để phân trang an toàn
        List<Movie> fullList = getMovies(genreId, null, page, pageSize);
        if (fullList == null || fullList.isEmpty()) {
            return Collections.emptyList();
        }
        return fullList;
    }

    /**
     * Thêm phim mới vào CSDL (phục vụ AdminMovieController).
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public boolean insertMovie(Movie m) {
        String sql =
            "INSERT INTO Movies (Title, OriginalTitle, Description, Duration, ReleaseYear, Country, Director, " +
            "Actors, PosterUrl, BackdropUrl, TrailerUrl, MovieType, IsVipOnly, IsRentOnly, RentalPrice, Status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PUBLISHED')";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getOriginalTitle());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getDuration());
            if (m.getReleaseYear() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, m.getReleaseYear());
            }
            ps.setString(6, m.getCountry());
            ps.setString(7, m.getDirector());
            ps.setString(8, m.getActors());
            ps.setString(9, m.getPosterUrl());
            ps.setString(10, m.getBackdropUrl());
            ps.setString(11, m.getTrailerUrl());
            ps.setString(
                12,
                m.getMovieType() != null ? m.getMovieType() : "SERIES"
            );
            ps.setBoolean(13, m.isVipOnly());
            ps.setBoolean(14, m.isRentOnly());
            ps.setBigDecimal(15, m.getRentalPrice());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm phim mới: " + m.getTitle(), e);
        }
        return false;
    }

    /**
     * Cập nhật thông tin phim (phục vụ AdminMovieController).
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public boolean updateMovie(Movie m) {
        String sql =
            "UPDATE Movies SET Title=?, OriginalTitle=?, Description=?, Duration=?, ReleaseYear=?, Country=?, " +
            "Director=?, Actors=?, PosterUrl=?, BackdropUrl=?, TrailerUrl=?, MovieType=?, IsVipOnly=?, " +
            "IsRentOnly=?, RentalPrice=?, UpdatedAt=GETDATE() WHERE MovieId=?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getOriginalTitle());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getDuration());
            if (m.getReleaseYear() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, m.getReleaseYear());
            }
            ps.setString(6, m.getCountry());
            ps.setString(7, m.getDirector());
            ps.setString(8, m.getActors());
            ps.setString(9, m.getPosterUrl());
            ps.setString(10, m.getBackdropUrl());
            ps.setString(11, m.getTrailerUrl());
            ps.setString(
                12,
                m.getMovieType() != null ? m.getMovieType() : "SERIES"
            );
            ps.setBoolean(13, m.isVipOnly());
            ps.setBoolean(14, m.isRentOnly());
            ps.setBigDecimal(15, m.getRentalPrice());
            ps.setInt(16, m.getMovieId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi cập nhật phim id=" + m.getMovieId(),
                e
            );
        }
        return false;
    }

    /**
     * Xóa mềm phim (đặt Status = 'DELETED').
     * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
     */
    public boolean softDelete(int movieId) {
        String sql =
            "UPDATE Movies SET Status = 'DELETED', UpdatedAt = GETDATE() WHERE MovieId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa mềm phim id=" + movieId, e);
        }
        return false;
    }

    private Movie map(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("MovieId"));
        m.setTitle(rs.getString("Title"));
        m.setOriginalTitle(rs.getString("OriginalTitle"));
        m.setDescription(rs.getString("Description"));
        m.setDuration(rs.getInt("Duration"));
        int y = rs.getInt("ReleaseYear");
        m.setReleaseYear(rs.wasNull() ? null : y);
        m.setCountry(rs.getString("Country"));
        m.setDirector(rs.getString("Director"));
        m.setActors(rs.getString("Actors"));
        m.setPosterUrl(rs.getString("PosterUrl"));
        m.setBackdropUrl(rs.getString("BackdropUrl"));
        m.setTrailerUrl(rs.getString("TrailerUrl"));
        m.setMovieType(rs.getString("MovieType"));
        m.setAgeRating(rs.getString("AgeRating"));
        m.setAudioSummary(rs.getString("AudioSummary"));
        m.setCurrentEpisode(rs.getInt("CurrentEpisode"));
        m.setTotalEpisodes(rs.getInt("TotalEpisodes"));
        m.setVipOnly(rs.getBoolean("IsVipOnly"));
        m.setRentOnly(rs.getBoolean("IsRentOnly"));
        m.setRentalPrice(rs.getBigDecimal("RentalPrice"));
        m.setStatus(rs.getString("Status"));
        return m;
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve all. */
    public List<Movie> getAll(String keyword, String status)
        throws SQLException {
        String sql =
            "SELECT * FROM Movies WHERE (? IS NULL OR Title LIKE ?) AND (? IS NULL OR Status=?) ORDER BY CreatedAt DESC";
        List<Movie> list = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement(sql)
        ) {
            String q =
                keyword == null || keyword.trim().isEmpty()
                    ? null
                    : keyword.trim();
            ps.setString(1, q);
            ps.setString(2, q == null ? null : "%" + q + "%");
            ps.setString(3, status == null || status.isEmpty() ? null : status);
            ps.setString(4, status == null || status.isEmpty() ? null : status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the insert operation. */
    public void insert(Movie m) throws SQLException {
        String sql =
            "INSERT INTO Movies(Title,OriginalTitle,Description,Duration,ReleaseYear,Country,Director,Actors,PosterUrl,BackdropUrl,TrailerUrl,MovieType,AgeRating,AudioSummary,CurrentEpisode,TotalEpisodes,IsVipOnly,IsRentOnly,RentalPrice,Status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement(sql)
        ) {
            bind(ps, m);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không tạo được phim mới");
            }
        }
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the update operation. */
    public void update(Movie m) throws SQLException {
        String sql =
            "UPDATE Movies SET Title=?,OriginalTitle=?,Description=?,Duration=?,ReleaseYear=?,Country=?,Director=?,Actors=?,PosterUrl=?,BackdropUrl=?,TrailerUrl=?,MovieType=?,AgeRating=?,AudioSummary=?,CurrentEpisode=?,TotalEpisodes=?,IsVipOnly=?,IsRentOnly=?,RentalPrice=?,Status=?,UpdatedAt=GETDATE() WHERE MovieId=? AND Status<>'DELETED'";
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement(sql)
        ) {
            bind(ps, m);
            ps.setInt(21, m.getMovieId());
            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "Không tìm thấy phim cần cập nhật: " + m.getMovieId()
                );
            }
        }
    }

    private void bind(PreparedStatement ps, Movie m) throws SQLException {
        ps.setString(1, m.getTitle());
        ps.setString(2, m.getOriginalTitle());
        ps.setString(3, m.getDescription());
        ps.setInt(4, m.getDuration());
        if (m.getReleaseYear() == null) {
            ps.setNull(5, Types.INTEGER);
        } else {
            ps.setInt(5, m.getReleaseYear());
        }
        ps.setString(6, m.getCountry());
        ps.setString(7, m.getDirector());
        ps.setString(8, m.getActors());
        ps.setString(9, m.getPosterUrl());
        ps.setString(10, m.getBackdropUrl());
        ps.setString(11, m.getTrailerUrl());
        ps.setString(12, m.getMovieType());
        ps.setString(13, m.getAgeRating());
        ps.setString(14, m.getAudioSummary());
        ps.setInt(15, m.getCurrentEpisode() > 0 ? m.getCurrentEpisode() : 1);
        ps.setInt(16, m.getTotalEpisodes() > 0 ? m.getTotalEpisodes() : 1);
        ps.setBoolean(17, m.isVipOnly());
        ps.setBoolean(18, m.isRentOnly());
        ps.setBigDecimal(19, m.getRentalPrice());
        ps.setString(20, m.getStatus() == null ? "PUBLISHED" : m.getStatus());
    }

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: count all. */
    public int countAll() throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM Movies WHERE Status<>'DELETED'"
            );
            ResultSet rs = ps.executeQuery()
        ) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
