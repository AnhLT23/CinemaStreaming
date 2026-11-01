package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Banner;

/**
 * Owner: Chuẩn / Hữu Lộc.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/BannerDAO.java.
 */
public class BannerDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        BannerDAO.class.getName()
    );

    /**
     * Lấy danh sách banner HOME_HERO đang hoạt động theo thời gian hiệu lực.
     * Kết hợp với Movies để lấy mô tả chi tiết phim cho Hero Carousel.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public List<Banner> getActiveHeroBanners() {
        List<Banner> list = new ArrayList<>();
        String sql =
            "SELECT b.BannerId, b.Title, b.ImageUrl, b.TargetUrl, b.Position, b.MovieId, " +
            "       b.StartDate, b.EndDate, b.IsActive, b.CreatedAt, " +
            "       m.Description AS MovieDescription, m.Title AS MovieTitle " +
            "FROM Banners b " +
            "LEFT JOIN Movies m ON b.MovieId = m.MovieId " +
            "WHERE b.IsActive = 1 " +
            "  AND b.Position = 'HOME_HERO' " +
            "  AND b.StartDate <= GETDATE() " +
            "  AND b.EndDate >= GETDATE() " +
            "ORDER BY b.CreatedAt DESC, b.BannerId DESC";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Banner b = new Banner();
                b.setBannerId(rs.getInt("BannerId"));
                b.setTitle(rs.getString("Title"));
                b.setImageUrl(rs.getString("ImageUrl"));
                b.setTargetUrl(rs.getString("TargetUrl"));
                b.setPosition(rs.getString("Position"));

                int movieId = rs.getInt("MovieId");
                if (!rs.wasNull()) {
                    b.setMovieId(movieId);
                }

                b.setStartDate(rs.getTimestamp("StartDate"));
                b.setEndDate(rs.getTimestamp("EndDate"));
                b.setActive(rs.getBoolean("IsActive"));
                b.setCreatedAt(rs.getTimestamp("CreatedAt"));

                String movieDesc = rs.getString("MovieDescription");
                b.setDescription(
                    movieDesc != null && !movieDesc.trim().isEmpty()
                        ? movieDesc
                        : b.getTitle()
                );
                b.setMovieTitle(rs.getString("MovieTitle"));

                list.add(b);
            }
        } catch (SQLException e) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi khi lấy danh sách banner HOME_HERO",
                e
            );
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh sách banner cho trang quản trị.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public List<Banner> getAllBanners() {
        List<Banner> list = new ArrayList<>();
        String sql =
            "SELECT BannerId, Title, ImageUrl, TargetUrl, Position, MovieId, StartDate, EndDate, IsActive, CreatedAt " +
            "FROM Banners ORDER BY BannerId DESC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Banner b = new Banner();
                b.setBannerId(rs.getInt("BannerId"));
                b.setTitle(rs.getString("Title"));
                b.setImageUrl(rs.getString("ImageUrl"));
                b.setTargetUrl(rs.getString("TargetUrl"));
                b.setPosition(rs.getString("Position"));
                int movId = rs.getInt("MovieId");
                b.setMovieId(rs.wasNull() ? null : movId);
                b.setStartDate(rs.getTimestamp("StartDate"));
                b.setEndDate(rs.getTimestamp("EndDate"));
                b.setActive(rs.getBoolean("IsActive"));
                b.setCreatedAt(rs.getTimestamp("CreatedAt"));
                list.add(b);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách tất cả banner", e);
        }
        return list;
    }

    /**
     * Thêm banner quảng cáo mới.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean insertBanner(
        String title,
        String imageUrl,
        String targetUrl,
        String position
    ) {
        String sql =
            "INSERT INTO Banners (Title, ImageUrl, TargetUrl, Position, StartDate, EndDate, IsActive, CreatedAt) " +
            "VALUES (?, ?, ?, ?, GETDATE(), DATEADD(YEAR, 1, GETDATE()), 1, GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, title);
            ps.setString(2, imageUrl);
            ps.setString(3, targetUrl);
            ps.setString(
                4,
                position == null || position.isBlank() ? "HOME_HERO" : position
            );
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm banner mới", e);
        }
        return false;
    }

    /**
     * Xóa banner theo BannerId.
     * Owner: Chuẩn / Hữu Lộc.
     */
    public boolean deleteBanner(int bannerId) {
        String sql = "DELETE FROM Banners WHERE BannerId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, bannerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa bannerId=" + bannerId, e);
        }
        return false;
    }

    private Banner map(ResultSet r) throws SQLException {
        Banner b = new Banner();
        b.setBannerId(r.getInt("BannerId"));
        int mid = r.getInt("MovieId");
        b.setMovieId(r.wasNull() ? null : mid);
        b.setTitle(r.getString("Title"));
        b.setImageUrl(r.getString("ImageUrl"));
        b.setTargetUrl(r.getString("TargetUrl"));
        b.setPosition(r.getString("Position"));
        b.setStartDate(r.getTimestamp("StartDate"));
        b.setEndDate(r.getTimestamp("EndDate"));
        b.setActive(r.getBoolean("IsActive"));
        return b;
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: retrieve by id. */
    public Banner getById(int id) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "SELECT * FROM Banners WHERE BannerId=?"
            )
        ) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: retrieve all. */
    public List<Banner> getAll() throws SQLException {
        List<Banner> a = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "SELECT * FROM Banners ORDER BY CreatedAt DESC"
            );
            ResultSet r = p.executeQuery()
        ) {
            while (r.next()) {
                a.add(map(r));
            }
        }
        return a;
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: perform the insert operation. */
    public void insert(Banner b) throws SQLException {
        String q =
            "INSERT INTO Banners(Title,ImageUrl,TargetUrl,Position,MovieId,StartDate,EndDate,IsActive) VALUES(?,?,?,?,?,?,?,?)";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            bind(p, b);
            if (p.executeUpdate() != 1) {
                throw new SQLException("Không tạo được banner");
            }
        }
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: perform the update operation. */
    public void update(Banner b) throws SQLException {
        String q =
            "UPDATE Banners SET Title=?,ImageUrl=?,TargetUrl=?,Position=?,MovieId=?,StartDate=?,EndDate=?,IsActive=? WHERE BannerId=?";
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(q)
        ) {
            bind(p, b);
            p.setInt(9, b.getBannerId());
            if (p.executeUpdate() != 1) {
                throw new SQLException(
                    "Không tìm thấy banner cần cập nhật: " + b.getBannerId()
                );
            }
        }
    }

    private void bind(PreparedStatement p, Banner b) throws SQLException {
        p.setString(1, b.getTitle());
        p.setString(2, b.getImageUrl());
        p.setString(3, b.getTargetUrl());
        p.setString(4, b.getPosition());
        if (b.getMovieId() == null) {
            p.setNull(5, Types.INTEGER);
        } else {
            p.setInt(5, b.getMovieId());
        }
        p.setTimestamp(6, b.getStartDate());
        p.setTimestamp(7, b.getEndDate());
        p.setBoolean(8, b.isActive());
    }

    /** Owner: Chuẩn / Hữu Lộc. Purpose: perform the delete operation. */
    public boolean delete(int id) throws SQLException {
        try (
            Connection c = getConnection();
            PreparedStatement p = c.prepareStatement(
                "DELETE FROM Banners WHERE BannerId=?"
            )
        ) {
            p.setInt(1, id);
            return p.executeUpdate() == 1;
        }
    }
}
