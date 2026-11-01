package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owner: Tuấn Anh.
 * Role: SQL Server data-access object for the feature module.
 * Source: src/main/java/dao/IssueReportDAO.java.
 */
public class IssueReportDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(
        IssueReportDAO.class.getName()
    );

    /** Owner: Tuấn Anh. Purpose: create report. */
    public boolean createReport(
        int episodeId,
        Integer serverId,
        Integer userId,
        String issueType,
        String description
    ) {
        String sql =
            "INSERT INTO IssueReports (EpisodeId, ServerId, UserId, IssueType, Description, Status, CreatedAt) " +
            "VALUES (?, ?, ?, ?, ?, 'OPEN', GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, episodeId);
            if (serverId != null && serverId > 0) {
                ps.setInt(2, serverId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (userId != null && userId > 0) {
                ps.setInt(3, userId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(
                4,
                issueType == null || issueType.isBlank()
                    ? "DEAD_LINK"
                    : issueType.trim()
            );
            ps.setString(
                5,
                description != null
                    ? description.trim()
                    : "Người xem báo lỗi luồng phát."
            );
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(
                Level.SEVERE,
                "Lỗi tạo báo cáo sự cố cho episodeId=" + episodeId,
                ex
            );
        }
        return false;
    }
}
