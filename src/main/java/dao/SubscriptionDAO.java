package dao;

import db.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Movie;
import model.SubscriptionPlan;

/**
 * Owner: Thông.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/SubscriptionDAO.java.
 */
public class SubscriptionDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        SubscriptionDAO.class.getName()
    );

    /** Lấy danh sách các gói cước VIP (SVOD) đang hoạt động.
     * Owner: Thông.
     */
    public List<SubscriptionPlan> getActivePlans() {
        List<SubscriptionPlan> list = new ArrayList<>();
        String sql =
            "SELECT PlanId, PlanCode, PlanName, Price, DurationDays, MaxScreens, MaxQuality, Description, IsActive " +
            "FROM SubscriptionPlans WHERE IsActive = 1 ORDER BY Price ASC";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapPlan(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách gói cước SVOD", e);
        }
        return list;
    }

    /** Lấy thông tin một gói cước theo PlanId.
     * Owner: Thông.
     */
    public SubscriptionPlan getById(int planId) {
        String sql =
            "SELECT PlanId, PlanCode, PlanName, Price, DurationDays, MaxScreens, MaxQuality, Description, IsActive " +
            "FROM SubscriptionPlans WHERE PlanId = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPlan(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy gói cước theo id=" + planId, e);
        }
        return null;
    }

    private SubscriptionPlan mapPlan(ResultSet rs) throws SQLException {
        SubscriptionPlan p = new SubscriptionPlan();
        p.setPlanId(rs.getInt("PlanId"));
        p.setPlanCode(rs.getString("PlanCode"));
        p.setPlanName(rs.getString("PlanName"));
        p.setPrice(rs.getDouble("Price"));
        p.setDurationDays(rs.getInt("DurationDays"));
        p.setMaxScreens(rs.getInt("MaxScreens"));
        p.setMaxQuality(rs.getString("MaxQuality"));
        p.setDescription(rs.getString("Description"));
        p.setActive(rs.getBoolean("IsActive"));
        return p;
    }

    /** Owner: Thông. Purpose: find active plans. */
    public List<SubscriptionPlan> findActivePlans() throws SQLException {
        String sql =
            "SELECT PlanId, PlanCode, PlanName, Price, DurationDays, MaxScreens, " +
            "MaxQuality, Description, IsActive FROM SubscriptionPlans " +
            "WHERE IsActive = 1 ORDER BY Price, DurationDays";
        List<SubscriptionPlan> plans = new ArrayList<>();
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                plans.add(mapPlan(rs));
            }
        }
        return plans;
    }

    /** Owner: Thông. Purpose: find theatrical movies. */
    public List<Movie> findTheatricalMovies() throws SQLException {
        String sql =
            "SELECT MovieId, Title, RentalPrice, Status, IsRentOnly " +
            "FROM Movies WHERE IsRentOnly = 1 AND Status = 'PUBLISHED' " +
            "ORDER BY ReleaseYear DESC, MovieId DESC";
        List<Movie> movies = new ArrayList<>();
        try (
            Connection con = new DBContext().getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("MovieId"));
                movie.setTitle(rs.getString("Title"));
                movie.setRentalPrice(rs.getDouble("RentalPrice"));
                movie.setStatus(rs.getString("Status"));
                movie.setRentOnly(rs.getBoolean("IsRentOnly"));
                movies.add(movie);
            }
        }
        return movies;
    }

    /** Owner: Thông. Purpose: find active plan. */
    public SubscriptionPlan findActivePlan(Connection con, int planId)
        throws SQLException {
        String sql =
            "SELECT PlanId, PlanCode, PlanName, Price, DurationDays, MaxScreens, " +
            "MaxQuality, Description, IsActive FROM SubscriptionPlans " +
            "WHERE PlanId = ? AND IsActive = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPlan(rs) : null;
            }
        }
    }

    /** Owner: Thông. Purpose: find published rental price. */
    public BigDecimal findPublishedRentalPrice(Connection con, int movieId)
        throws SQLException {
        String sql =
            "SELECT RentalPrice FROM Movies " +
            "WHERE MovieId = ? AND IsRentOnly = 1 AND Status = 'PUBLISHED'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("RentalPrice") : null;
            }
        }
    }

    /** Owner: Thông. Purpose: check whether active vip. */
    public boolean isActiveVip(Connection con, int userId) throws SQLException {
        String sql =
            "SELECT 1 FROM Users " +
            "WHERE UserId = ? AND IsVip = 1 AND VipExpireDate > SYSDATETIME()";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
