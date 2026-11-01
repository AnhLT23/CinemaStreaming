package model;

import java.io.Serializable;

/**
 * Owner: Tuấn Anh.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/EpisodeServer.java.
 */
public class EpisodeServer implements Serializable {

    private static final long serialVersionUID = 1L;

    private int serverId;
    private int episodeId;
    private String serverName;
    private String serverType;
    private String quality;
    private String videoUrl;
    private String subtitleUrl;
    private String audioType;
    private boolean isVipServer;
    private int priority;

    public EpisodeServer() {}

    public EpisodeServer(
        int serverId,
        int episodeId,
        String serverName,
        String serverType,
        String quality,
        String videoUrl,
        String subtitleUrl,
        String audioType,
        boolean isVipServer,
        int priority
    ) {
        this.serverId = serverId;
        this.episodeId = episodeId;
        this.serverName = serverName;
        this.serverType = serverType;
        this.quality = quality;
        this.videoUrl = videoUrl;
        this.subtitleUrl = subtitleUrl;
        this.audioType = audioType;
        this.isVipServer = isVipServer;
        this.priority = priority;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getId() {
        return serverId;
    }

    public void setId(int id) {
        this.serverId = id;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getName() {
        return serverName;
    }

    public void setName(String name) {
        this.serverName = name;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getStreamUrl() {
        return videoUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.videoUrl = streamUrl;
    }

    public String getSubtitleUrl() {
        return subtitleUrl;
    }

    public void setSubtitleUrl(String subtitleUrl) {
        this.subtitleUrl = subtitleUrl;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public boolean isVipServer() {
        return isVipServer;
    }

    public void setVipServer(boolean isVipServer) {
        this.isVipServer = isVipServer;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
