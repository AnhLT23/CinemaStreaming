<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý tập phim | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section admin-page">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />
        <div class="section-heading">
            <div>
                <span class="eyebrow">ADMIN CONSOLE</span>
                <h1>Tập phim &amp; nguồn phát</h1>
            </div>
        </div>
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin">Tổng quan</a>
            <a href="${pageContext.request.contextPath}/admin/movies">Phim</a>
            <a class="active" href="${pageContext.request.contextPath}/admin/episodes">Tập phim</a>
            <a href="${pageContext.request.contextPath}/admin/banners">Banner</a>
            <a href="${pageContext.request.contextPath}/admin/comments">Bình luận</a>
            <a href="${pageContext.request.contextPath}/admin/members">Hội viên</a>
        </nav>

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${error}" />
            </div>
        </c:if>

        <form class="filter-bar row g-2 mb-4" method="get" action="${pageContext.request.contextPath}/admin/episodes">
            <div class="col-md-6">
                <label class="visually-hidden" for="movieSelect">Chọn phim</label>
                <select class="form-select" id="movieSelect" name="movieId" required>
                    <option value="">Chọn phim cần quản lý tập</option>
                    <c:forEach items="${movies}" var="movie">
                        <option value="${movie.id}"
                            ${not empty selectedMovie and selectedMovie.id eq movie.id ? 'selected' : ''}>
                            <c:out value="${movie.title}" />
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto">
                <button class="btn btn-noir-outline" type="submit">
                    Quản lý tập
                </button>
            </div>
        </form>

        <c:if test="${not empty selectedMovie}">
            <section class="panel mb-4">
                <h2 class="h4">
                    Thêm tập phim ·
                    <c:out value="${selectedMovie.title}" />
                </h2>
                <p class="text-secondary">
                    Tạo thông tin tập trước, sau đó thêm URL nguồn phát cho tập đó. Hệ thống hiện lưu URL video,
                    không tải tệp video lên máy chủ.
                </p>
                <form action="${pageContext.request.contextPath}/admin/episodes" method="post" class="form-grid">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                    <input type="hidden" name="action" value="addEpisode" />
                    <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                    <div>
                        <label class="form-label" for="episodeNumber">Số tập</label>
                        <input class="form-control" id="episodeNumber" type="number" min="1" name="episodeNumber"
                            required />
                    </div>
                    <div>
                        <label class="form-label" for="episodeTitle">Tên tập</label>
                        <input class="form-control" id="episodeTitle" name="episodeTitle" maxlength="200" required />
                    </div>
                    <div>
                        <label class="form-label" for="durationSeconds">Thời lượng (giây)</label>
                        <input class="form-control" id="durationSeconds" type="number" min="0" name="durationSeconds"
                            value="0" required />
                    </div>
                    <div class="full-width">
                        <button class="btn btn-accent" type="submit">
                            Thêm tập
                        </button>
                    </div>
                </form>
            </section>

            <c:if test="${empty episodes}">
                <p class="empty-state">Phim này chưa có tập nào.</p>
            </c:if>
            <c:forEach items="${episodes}" var="episode">
                <section class="panel mb-4">
                    <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                        <div>
                            <h2 class="h5 mb-1">
                                Tập ${episode.episodeNumber} ·
                                <c:out value="${episode.episodeTitle}" />
                            </h2>
                            <span class="text-secondary">${episode.durationSeconds} giây</span>
                        </div>
                        <form action="${pageContext.request.contextPath}/admin/episodes" method="post"
                            onsubmit="return confirm('Xóa tập phim này và toàn bộ nguồn phát?')">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                            <input type="hidden" name="action" value="deleteEpisode" />
                            <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                            <input type="hidden" name="episodeId" value="${episode.episodeId}" />
                            <button class="btn btn-sm btn-outline-danger" type="submit">
                                Xóa tập
                            </button>
                        </form>
                    </div>

                    <div class="table-responsive mb-3">
                        <table class="table noir-table">
                            <thead>
                                <tr>
                                    <th>Tên nguồn</th>
                                    <th>Loại</th>
                                    <th>Chất lượng</th>
                                    <th>URL</th>
                                    <th>Ưu tiên</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${episode.servers}" var="server">
                                    <tr>
                                        <td>
                                            <c:out value="${server.serverName}" />
                                        </td>
                                        <td>
                                            <c:out value="${server.serverType}" />
                                        </td>
                                        <td>
                                            <c:out value="${server.quality}" />
                                        </td>
                                        <td class="url-cell">
                                            <c:out value="${server.videoUrl}" />
                                        </td>
                                        <td>${server.priority}</td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/admin/episodes"
                                                method="post" onsubmit="return confirm('Xóa nguồn phát này?')">
                                                <input type="hidden" name="csrfToken"
                                                    value="${sessionScope.csrfToken}" />
                                                <input type="hidden" name="action" value="deleteServer" />
                                                <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                                                <input type="hidden" name="serverId" value="${server.serverId}" />
                                                <button class="btn btn-sm btn-outline-danger" type="submit">
                                                    Xóa nguồn
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty episode.servers}">
                                    <tr>
                                        <td colspan="6" class="text-secondary">
                                            Chưa cấu hình nguồn phát.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <h3 class="h6">Thêm nguồn phát</h3>
                    <form action="${pageContext.request.contextPath}/admin/episodes" method="post" class="form-grid">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <input type="hidden" name="action" value="addServer" />
                        <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                        <input type="hidden" name="episodeId" value="${episode.episodeId}" />
                        <div>
                            <label class="form-label" for="serverName-${episode.episodeId}">Tên nguồn</label>
                            <input class="form-control" id="serverName-${episode.episodeId}" name="serverName"
                                maxlength="100" required />
                        </div>
                        <div>
                            <label class="form-label" for="serverType-${episode.episodeId}">Loại nguồn</label>
                            <select class="form-select" id="serverType-${episode.episodeId}" name="serverType"
                                required>
                                <option value="HLS_SPEED">HLS chính</option>
                                <option value="HLS_BACKUP">HLS dự phòng</option>
                                <option value="IFRAME_EMBED">Video nhúng</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label" for="quality-${episode.episodeId}">Chất lượng</label>
                            <select class="form-select" id="quality-${episode.episodeId}" name="quality" required>
                                <option value="720P">720P</option>
                                <option value="1080P_60FPS" selected>1080P 60FPS</option>
                                <option value="4K_UHD">4K UHD</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label" for="videoUrl-${episode.episodeId}">URL video</label>
                            <input class="form-control" id="videoUrl-${episode.episodeId}" type="url" name="videoUrl"
                                maxlength="1000" required />
                            <small class="form-text text-secondary">Dùng URL phát trực tiếp HTTPS hoặc URL nhúng phù hợp với loại nguồn.</small>
                        </div>
                        <div>
                            <label class="form-label" for="subtitleUrl-${episode.episodeId}">URL phụ đề (không bắt
                                buộc)</label>
                            <input class="form-control" id="subtitleUrl-${episode.episodeId}" type="url"
                                name="subtitleUrl" maxlength="500" />
                        </div>
                        <div>
                            <label class="form-label" for="audioType-${episode.episodeId}">Âm thanh</label>
                            <select class="form-select" id="audioType-${episode.episodeId}" name="audioType" required>
                                <option value="Thuyết minh">Thuyết minh</option>
                                <option value="Lồng tiếng">Lồng tiếng</option>
                                <option value="Vietsub">Vietsub</option>
                            </select>
                        </div>
                        <div>
                            <label class="form-label" for="priority-${episode.episodeId}">Thứ tự ưu tiên</label>
                            <input class="form-control" id="priority-${episode.episodeId}" type="number" min="1"
                                name="priority" value="1" required />
                        </div>
                        <div class="form-check align-self-end">
                            <input class="form-check-input" id="isVipServer-${episode.episodeId}" type="checkbox"
                                name="isVipServer" />
                            <label class="form-check-label" for="isVipServer-${episode.episodeId}">Nguồn VIP</label>
                        </div>
                        <div class="full-width">
                            <button class="btn btn-noir-outline" type="submit">
                                Thêm nguồn phát
                            </button>
                        </div>
                    </form>
                </section>
            </c:forEach>
        </c:if>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
