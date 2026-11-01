package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Owner: Tuấn Anh.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/IssueReport.java.
 */
public class IssueReport implements Serializable {

    private long reportId;
    private int episodeId;
    private Integer serverId;
    private Integer userId;
    private String issueType;
    private String description;
    private String status;
    private Timestamp createdAt;

    public IssueReport() {}

    public IssueReport(
        long reportId,
        int episodeId,
        Integer serverId,
        Integer userId,
        String issueType,
        String description,
        String status,
        Timestamp createdAt
    ) {
        this.reportId = reportId;
        this.episodeId = episodeId;
        this.serverId = serverId;
        this.userId = userId;
        this.issueType = issueType;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
