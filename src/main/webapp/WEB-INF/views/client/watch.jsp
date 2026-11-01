<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ep" value="${not empty currentEpisode ? currentEpisode : currentEp}" />
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>
        Xem phim
        <c:out value="${movie.title}" /> - Tập ${not empty ep ?
        ep.episodeNumber : 1} | TVHAY+
    </title>
    <link rel="stylesheet" href="${ctx}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${ctx}/assets/css/cinema-noir.css" />
    <link rel="stylesheet" href="${ctx}/assets/css/watch-page.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />

    <main class="tvhay-watch-wrapper">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />

        <%-- 1. THANH BREADCRUMB THEO ĐÚNG THIẾT KẾ FLIM.JPG --%>
        <nav class="tvhay-breadcrumb" aria-label="Breadcrumb">
            <a href="${ctx}/home">
                <span class="material-symbols-outlined" style="font-size:16px;">home</span>
                <span>Trang Chủ</span>
            </a>
            <span class="material-symbols-outlined" style="font-size:14px;">chevron_right</span>
            <a href="${ctx}/movies?type=${movie.movieType}">
                <c:choose>
                    <c:when test="${movie.movieType eq 'SINGLE'}">Phim Lẻ</c:when>
                    <c:when test="${movie.movieType eq 'ANIMATION_3D'}">Hoạt Hình 3D</c:when>
                    <c:otherwise>Phim Bộ</c:otherwise>
                </c:choose>
            </a>
            <span class="material-symbols-outlined" style="font-size:14px;">chevron_right</span>
            <a href="${ctx}/movie?id=${movie.movieId}" class="fw-bold" style="color:var(--tvhay-text);">
                <c:out value="${movie.title}" />
            </a>
            <span class="material-symbols-outlined" style="font-size:14px;">chevron_right</span>
            <span class="ep-pill-current">Tập ${not empty ep ? ep.episodeNumber : 1}</span>
        </nav>

        <%-- Dữ liệu đồng bộ ngầm Heartbeat 10s --%>
        <div id="watchSyncMeta" data-sync-url="${ctx}/api/history/sync"
            data-episode-id="${not empty ep ? ep.episodeId : 1}"
            data-duration-seconds="${not empty ep and ep.durationSeconds gt 0 ? ep.durationSeconds : 1458}"
            data-resume-second="${not empty resumeSecond ? resumeSecond : 0}"
            data-movie-detail-url="${ctx}/movie?id=${movie.movieId}" hidden></div>

        <%-- 2. LƯỚI 2 CỘT: 8 CỘT PLAYER + 4 CỘT SIDEBAR --%>
        <div class="tvhay-watch-grid">
            <%-- CỘT TRÁI (8/12): KHUNG XEM PHIM TRỰC TIẾP, ĐỔI SERVER & CHỌN TẬP --%>
            <section class="d-flex flex-column gap-3 min-w-0">
                <%-- KHUNG PLAYER 16:9 PHÁT TRỰC TIẾP --%>
                <div id="playerCanvas" class="tvhay-player-canvas">
                    <%-- Watermark góc trái --%>
                    <div class="tvhay-player-watermark">
                        <span class="fw-bolder text-uppercase"
                            style="font-family:'Be Vietnam Pro',sans-serif; font-size:13px; color:#ffffff;">
                            TV<span style="color:#ffc665;">HAY</span>
                        </span>
                        <span style="font-size:9px; font-weight:800; letter-spacing:0.1em; color:#9d8f7c;">STUDIO
                            VIP</span>
                    </div>

                    <%-- Nhãn chất lượng Live HUD góc phải --%>
                    <div class="tvhay-player-hud-tag">
                        <span class="tvhay-hud-dot"></span>
                        <span id="hudQualityText">${not empty activeServer.quality ? activeServer.quality :
                            '1080P • 60FPS'}</span>
                    </div>

                    <%-- Trình phát HTML5 Video (Hỗ trợ HLS .m3u8 & MP4 trực tiếp) --%>
                    <video id="videoPlayer" controls playsinline preload="metadata"
                        poster="${not empty movie.backdropUrl and movie.backdropUrl.startsWith('http') ? movie.backdropUrl : ctx.concat('/assets/img/banner-sample.png')}"
                        data-src="${not empty activeServer ? activeServer.videoUrl : 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'}"
                        data-resume-second="${not empty resumeSecond ? resumeSecond : 0}">
                        <c:if test="${not empty activeServer.subtitleUrl}">
                            <track kind="subtitles" src="${ctx}${activeServer.subtitleUrl}" srclang="vi"
                                label="Tiếng Việt" default />
                        </c:if>
                        Trình duyệt của bạn không hỗ trợ phát video HTML5.
                    </video>

                    <%-- Trình phát Iframe (Tự động bật khi chọn Server YouTube / Embed) --%>
                    <iframe id="embedPlayer" class="d-none" src="about:blank" title="Trình phát dự phòng"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        allowfullscreen></iframe>
                </div>

                <%-- THANH ĐIỀU KHIỂN NHANH & TƯƠNG TÁC --%>
                <div class="tvhay-module-card d-flex flex-wrap align-items-center justify-content-between gap-2">
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <span class="fw-semibold" style="font-size:15px;">
                            Đang xem:
                            <strong style="color:#ffc665;">
                                Tập ${not empty ep ? ep.episodeNumber : 1}
                                <c:if test="${not empty ep.episodeTitle}">
                                    -
                                    <c:out value="${ep.episodeTitle}" />
                                </c:if>
                            </strong>
                        </span>
                        <c:if test="${not empty resumeSecond and resumeSecond gt 0}">
                            <span class="badge rounded-pill"
                                style="background:rgba(255,198,101,0.18); color:#ffc665; font-size:11px;">
                                ▶ Tự động phát tiếp từ giây thứ ${resumeSecond}s
                            </span>
                        </c:if>
                    </div>

                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <button type="button" id="btnRewind10" class="tvhay-action-pill" title="Tua lùi 10 giây">
                            <span class="material-symbols-outlined" style="font-size:18px;">replay_10</span>
                            <span>-10s</span>
                        </button>
                        <button type="button" id="btnForward10" class="tvhay-action-pill" title="Tua tới 10 giây">
                            <span class="material-symbols-outlined" style="font-size:18px;">forward_10</span>
                            <span>+10s</span>
                        </button>
                        <button type="button" id="btnPlaybackSpeed" class="tvhay-action-pill" title="Tốc độ phát">
                            1.0x
                        </button>
                        <button type="button" id="btnTheaterMode" class="tvhay-action-pill d-none d-lg-inline-flex"
                            title="Chế độ rạp">
                            <span class="material-symbols-outlined" style="font-size:18px;">crop_landscape</span>
                            <span>Rạp</span>
                        </button>
                        <button type="button" id="btnFullscreen" class="tvhay-action-pill" title="Toàn màn hình">
                            <span class="material-symbols-outlined" style="font-size:18px;">fullscreen</span>
                        </button>
                        <button type="button" class="tvhay-action-pill"
                            onclick="document.getElementById('reportModalBox').classList.toggle('d-none');">
                            <span class="material-symbols-outlined" style="font-size:18px; color:#ffb4ab;">report</span>
                            <span>Báo lỗi</span>
                        </button>
                    </div>
                </div>

                <%-- KHỐI BÁO LỖI NHANH (ẨN MẶC ĐỊNH, BẤM NÚT BÁO LỖI SẼ HIỆN) --%>
                <form id="reportModalBox" action="${ctx}/watch" method="post" class="tvhay-module-card d-none">
                    <input type="hidden" name="action" value="report" />
                    <input type="hidden" name="movieId" value="${movie.movieId}" />
                    <input type="hidden" name="ep" value="${not empty ep ? ep.episodeNumber : 1}" />
                    <input type="hidden" name="episodeId" value="${not empty ep ? ep.episodeId : 1}" />
                    <div class="row g-2 align-items-center">
                        <div class="col-12 col-md-4">
                            <select name="issueType" class="form-select form-select-sm">
                                <option value="VIDEO_LAG">
                                    Video giật lag / Không tải được
                                </option>
                                <option value="AUDIO_DESYNC">
                                    Lệch âm thanh / Mất tiếng
                                </option>
                                <option value="SUBTITLE_ERROR">Lỗi phụ đề Vietsub</option>
                            </select>
                        </div>
                        <div class="col-12 col-md-6">
                            <input type="text" name="description" class="form-control form-control-sm"
                                placeholder="Mô tả chi tiết lỗi gặp phải (phút thứ mấy, server nào)..." />
                        </div>
                        <div class="col-12 col-md-2">
                            <button type="submit" class="tvhay-btn-gold w-100 py-1">
                                Gửi báo lỗi
                            </button>
                        </div>
                    </div>
                </form>

                <%-- MODULE CHUYỂN ĐỔI NGUỒN PHÁT MÁY CHỦ (MULTI-SERVER SWITCHER) --%>
                <div class="tvhay-module-card">
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <span class="tvhay-hud-dot" style="background:#e10111;"></span>
                            <span class="fw-bold text-uppercase" style="font-size:15px;">Nguồn Phát Máy Chủ</span>
                            <span class="px-2 py-1 rounded-pill"
                                style="background:rgba(225,1,17,0.22); color:#ffb4aa; font-size:10px; font-weight:800;">
                                HLS SPEED
                            </span>
                        </div>
                        <span style="font-size:12px; color:var(--tvhay-outline);">
                            Đổi server nếu video giật hoặc không tải được
                        </span>
                    </div>

                    <div class="tvhay-server-grid">
                        <c:forEach items="${servers}" var="sv" varStatus="st">
                            <button type="button"
                                class="tvhay-server-btn js-server-switch ${(not empty activeServer and activeServer.serverId eq sv.serverId) or (empty activeServer and st.first) ? 'active' : ''}"
                                data-server-id="${sv.serverId}" data-video-url="${fn:escapeXml(sv.videoUrl)}"
                                data-server-type="${sv.serverType}" data-quality="${sv.quality}"
                                data-subtitle-url="${not empty sv.subtitleUrl ? ctx.concat(sv.subtitleUrl) : ''}">
                                <span class="text-truncate">
                                    <span class="tvhay-server-dot"></span>
                                    <c:out value="${sv.serverName}" />
                                    <c:if test="${not empty sv.audioType}">
                                        (
                                        <c:out value="${sv.audioType}" />)
                                    </c:if>
                                </span>
                                <span class="tvhay-server-tag">
                                    <c:choose>
                                        <c:when test="${sv.serverType eq 'HLS_SPEED'}">NHANH</c:when>
                                        <c:when test="${sv.serverType eq 'IFRAME_EMBED'}">IFRAME</c:when>
                                        <c:otherwise>DỰ PHÒNG</c:otherwise>
                                    </c:choose>
                                </span>
                            </button>
                        </c:forEach>
                    </div>
                </div>

                <%-- MODULE DANH SÁCH TẬP DẠNG LƯỚI Ô VUÔNG --%>
                <div class="tvhay-module-card">
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined"
                                style="color:#ffc665; font-size:22px;">video_library</span>
                            <h2 class="h6 fw-bold m-0">Danh Sách Tập</h2>
                            <span class="px-2 py-1 rounded"
                                style="background:var(--tvhay-surface-high); color:#ffc665; font-size:10px; font-weight:800;">
                                TẬP 1 - ${fn:length(episodes)}
                            </span>
                        </div>
                        <div class="d-flex align-items-center gap-3"
                            style="font-size:12px; color:var(--tvhay-text-muted);">
                            <span class="d-inline-flex align-items-center gap-1">
                                <span style="width:8px; height:8px; border-radius:50%; background:#ffc665;"></span>
                                Đang xem
                            </span>
                            <span class="d-inline-flex align-items-center gap-1">
                                <span style="width:8px; height:8px; border-radius:50%; background:#33353b;"></span>
                                Chưa xem
                            </span>
                        </div>
                    </div>

                    <div class="tvhay-ep-grid">
                        <c:forEach items="${episodes}" var="item">
                            <c:url var="epWatchUrl" value="/watch">
                                <c:param name="movieId" value="${movie.movieId}" />
                                <c:param name="ep" value="${item.episodeNumber}" />
                            </c:url>
                            <a href="${epWatchUrl}"
                                class="tvhay-ep-btn ${(not empty ep and ep.episodeNumber eq item.episodeNumber) ? 'active' : ''}"
                                title="${item.episodeTitle}">
                                <c:choose>
                                    <c:when test="${item.episodeNumber lt 10}">0${item.episodeNumber}</c:when>
                                    <c:otherwise>${item.episodeNumber}</c:otherwise>
                                </c:choose>
                            </a>
                        </c:forEach>
                    </div>
                </div>
            </section>

            <%-- CỘT PHẢI (4/12): SIDEBAR THÔNG TIN PHIM & PHIM CÙNG THỂ LOẠI --%>
            <aside class="d-flex flex-column gap-3 w-100">
                <%-- THẺ THÔNG TIN CHI TIẾT PHIM --%>
                <div class="tvhay-module-card d-flex flex-column gap-3">
                    <div class="d-flex gap-3">
                        <div class="position-relative flex-shrink-0 rounded-3 overflow-hidden"
                            style="width:96px; aspect-ratio:2/3; background:#282a30;">
                            <c:set var="posterUrl"
                                value="${not empty movie.posterUrl and movie.posterUrl.startsWith('http') ? movie.posterUrl : ctx.concat('/assets/img/poster-sample.png')}" />
                            <img src="${posterUrl}" alt="${movie.title}" class="w-100 h-100 object-fit-cover"
                                onerror="this.onerror=null;this.src='${ctx}/assets/img/poster-sample.svg'" />
                            <span class="position-absolute top-0 start-0 m-1 px-1 rounded"
                                style="background:#ffc665; color:#432c00; font-size:10px; font-weight:900;">4K</span>
                        </div>

                        <div class="d-flex flex-column justify-content-between min-w-0">
                            <div>
                                <h1 class="h5 fw-bolder text-truncate m-0">
                                    <c:out value="${movie.title}" />
                                </h1>
                                <p class="small text-truncate mb-1" style="color:var(--tvhay-outline);">
                                    <c:out
                                        value="${not empty movie.originalTitle ? movie.originalTitle : movie.title}" />
                                </p>
                            </div>

                            <div class="d-flex align-items-center gap-2">
                                <span class="px-2 py-1 rounded fw-bold"
                                    style="background:var(--tvhay-surface-high); color:#ffc665; font-size:12px;">
                                    ★ ${not empty movie.avgRating ? movie.avgRating : '9.2'}
                                </span>
                                <span style="font-size:12px; color:var(--tvhay-text-muted);">
                                    ${not empty movie.totalReviews ? movie.totalReviews : 120}
                                    lượt đánh giá
                                </span>
                            </div>

                            <div class="d-flex flex-wrap gap-1 mt-1">
                                <span class="px-2 py-1 rounded"
                                    style="background:var(--tvhay-surface); font-size:11px;">
                                    ${movie.releaseYear}
                                </span>
                                <span class="px-2 py-1 rounded"
                                    style="background:var(--tvhay-surface); color:#ffc665; font-size:11px; font-weight:700;">
                                    Tập ${movie.currentEpisode} / ${movie.totalEpisodes}
                                </span>
                                <span class="px-2 py-1 rounded"
                                    style="background:var(--tvhay-surface); font-size:11px;">
                                    <c:out value="${not empty movie.country ? movie.country : 'Quốc tế'}" />
                                </span>
                            </div>
                        </div>
                    </div>

                    <%-- Bảng thuộc tính Metadata --%>
                    <div class="row g-2 p-2 rounded-3 m-0" style="background:var(--tvhay-surface); font-size:12px;">
                        <div class="col-6">
                            <span class="d-block" style="color:var(--tvhay-outline); font-size:11px;">Âm thanh</span>
                            <strong class="text-truncate d-block">
                                <c:out
                                    value="${not empty movie.audioSummary ? movie.audioSummary : 'Thuyết minh + Vietsub'}" />
                            </strong>
                        </div>
                        <div class="col-6">
                            <span class="d-block" style="color:var(--tvhay-outline); font-size:11px;">Trạng thái</span>
                            <strong style="color:#ffc665;">Đang phát sóng</strong>
                        </div>
                        <div class="col-12 pt-1">
                            <span class="d-block" style="color:var(--tvhay-outline); font-size:11px;">Diễn viên</span>
                            <span>
                                <c:out value="${not empty movie.actors ? movie.actors : 'Đang cập nhật'}" />
                            </span>
                        </div>
                    </div>

                    <%-- Tóm tắt nội dung --%>
                    <div>
                        <span class="d-block fw-bold text-uppercase mb-1"
                            style="font-size:12px; letter-spacing:0.05em;">
                            Tóm tắt nội dung
                        </span>
                        <p class="m-0" style="font-size:13px; color:var(--tvhay-text-muted); line-height:1.65;">
                            <c:out value="${movie.description}" />
                        </p>
                    </div>
                </div>

                <%-- KHỐI PHIM CÙNG THỂ LOẠI --%>
                <div class="tvhay-module-card d-flex flex-column gap-3">
                    <div class="d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-1">
                            <span class="material-symbols-outlined"
                                style="color:#ffc665; font-size:20px;">auto_awesome_motion</span>
                            <h3 class="h6 fw-bold m-0">Phim Cùng Thể Loại</h3>
                        </div>
                        <a href="${ctx}/movies"
                            style="font-size:12px; color:#ffc665; text-decoration:none; font-weight:700;">Xem thêm</a>
                    </div>

                    <div class="row row-cols-2 g-2">
                        <c:forEach items="${relatedMovies}" var="rel">
                            <div class="col">
                                <a href="${ctx}/watch?movieId=${rel.movieId}"
                                    class="d-block p-1 rounded-3 text-decoration-none"
                                    style="background:var(--tvhay-surface);">
                                    <div class="position-relative rounded-2 overflow-hidden mb-1"
                                        style="aspect-ratio:16/10; background:#282a30;">
                                        <c:set var="relImg"
                                            value="${not empty rel.backdropUrl and rel.backdropUrl.startsWith('http') ? rel.backdropUrl : (not empty rel.posterUrl and rel.posterUrl.startsWith('http') ? rel.posterUrl : ctx.concat('/assets/img/poster-sample.png'))}" />
                                        <img src="${relImg}" alt="${rel.title}" class="w-100 h-100 object-fit-cover"
                                            onerror="this.onerror=null;this.src='${ctx}/assets/img/poster-sample.svg'" />
                                        <span class="position-absolute top-0 start-0 m-1 px-1 rounded"
                                            style="background:#ffc665; color:#432c00; font-size:9px; font-weight:800;">HD</span>
                                    </div>
                                    <div class="px-1">
                                        <div class="fw-semibold text-truncate"
                                            style="font-size:12px; color:var(--tvhay-text);">
                                            <c:out value="${rel.title}" />
                                        </div>
                                        <div class="text-truncate" style="font-size:11px; color:var(--tvhay-outline);">
                                            Thuyết minh • ${rel.releaseYear}
                                        </div>
                                    </div>
                                </a>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </aside>
        </div>
    </main>

    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${ctx}/assets/js/bootstrap.bundle.min.js"></script>
    <script src="${ctx}/assets/js/hls.min.js"></script>
    <script src="${ctx}/assets/js/player.js"></script>
    <script src="${ctx}/assets/js/sync-watch.js"></script>
</body>

</html>
