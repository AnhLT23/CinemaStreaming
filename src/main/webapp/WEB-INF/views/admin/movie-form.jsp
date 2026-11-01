<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${empty movie.id ? 'Thêm phim' : 'Cập nhật phim'} | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section form-page">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />
        <a class="back-link" href="${pageContext.request.contextPath}/admin/movies">← Danh sách phim</a>
        <div class="section-heading mt-3">
            <div>
                <span class="eyebrow">ADMIN CONSOLE</span>
                <h1>${empty movie.id ? 'Thêm phim mới' : 'Chỉnh sửa phim'}</h1>
            </div>
        </div>
        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <c:out value="${error}" />
            </div>
        </c:if>
        <form action="${pageContext.request.contextPath}/admin/movies" method="post" class="panel form-grid" novalidate>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input type="hidden" name="action"
                value="save" /><input type="hidden" name="movieId" value="${movie.id}" />
            <div>
                <label class="form-label" for="title">Tên phim *</label><input id="title" class="form-control"
                    name="title" value="${fn:escapeXml(movie.title)}" maxlength="200" required />
            </div>
            <div>
                <label class="form-label" for="originalTitle">Tên gốc</label><input id="originalTitle"
                    class="form-control" name="originalTitle" value="${fn:escapeXml(movie.originalTitle)}"
                    maxlength="200" />
            </div>
            <div class="full-width">
                <label class="form-label" for="description">Mô tả</label><textarea id="description" class="form-control"
                    name="description" rows="4" maxlength="5000">
<c:out value="${movie.description}" /></textarea>
            </div>
            <div>
                <label class="form-label" for="releaseYear">Năm phát hành</label><input id="releaseYear"
                    class="form-control" name="releaseYear" type="number" min="1888" max="2100"
                    value="${movie.releaseYear}" />
            </div>
            <div>
                <label class="form-label" for="duration">Thời lượng (phút) *</label><input id="duration"
                    class="form-control" name="duration" type="number" min="1" max="1000" value="${movie.duration}"
                    required />
            </div>
            <div>
                <label class="form-label" for="country">Quốc gia</label><input id="country" class="form-control"
                    name="country" value="${fn:escapeXml(movie.country)}" maxlength="100" />
            </div>
            <div>
                <label class="form-label" for="director">Đạo diễn</label><input id="director" class="form-control"
                    name="director" value="${fn:escapeXml(movie.director)}" maxlength="200" />
            </div>
            <div class="full-width">
                <label class="form-label" for="actors">Diễn viên</label><input id="actors" class="form-control"
                    name="actors" value="${fn:escapeXml(movie.actors)}" maxlength="1000" />
            </div>
            <div>
                <label class="form-label" for="posterUrl">Đường dẫn poster</label><input id="posterUrl"
                    class="form-control" name="posterUrl" value="${fn:escapeXml(movie.posterUrl)}" maxlength="500" />
            </div>
            <div>
                <label class="form-label" for="backdropUrl">Đường dẫn backdrop</label><input id="backdropUrl"
                    class="form-control" name="backdropUrl" value="${fn:escapeXml(movie.backdropUrl)}"
                    maxlength="500" />
            </div>
            <div>
                <label class="form-label" for="trailerUrl">URL trailer</label><input id="trailerUrl"
                    class="form-control" name="trailerUrl" type="url" value="${fn:escapeXml(movie.trailerUrl)}" />
            </div>
            <div>
                <label class="form-label" for="movieType">Loại phim</label><select id="movieType" class="form-select"
                    name="movieType">
                    <option value="SERIES" ${movie.movieType eq 'SERIES' ? 'selected' : '' }>
                        Phim bộ
                    </option>
                    <option value="SINGLE" ${movie.movieType eq 'SINGLE' ? 'selected' : '' }>
                        Phim lẻ
                    </option>
                    <option value="ANIMATION_3D" ${movie.movieType eq 'ANIMATION_3D' ? 'selected' : '' }>
                        Hoạt hình 3D
                    </option>
                </select>
            </div>
            <div>
                <label class="form-label" for="ageRating">Giới hạn độ tuổi</label><input id="ageRating"
                    class="form-control" name="ageRating" value="${fn:escapeXml(movie.ageRating)}" maxlength="20" />
            </div>
            <div class="full-width">
                <label class="form-label" for="audioSummary">Thông tin âm thanh/phụ đề</label><input id="audioSummary"
                    class="form-control" name="audioSummary" value="${fn:escapeXml(movie.audioSummary)}"
                    maxlength="500" />
            </div>
            <div>
                <label class="form-label" for="currentEpisode">Tập hiện tại</label><input id="currentEpisode"
                    class="form-control" name="currentEpisode" type="number" min="1" value="${movie.currentEpisode}" />
            </div>
            <div>
                <label class="form-label" for="totalEpisodes">Tổng số tập</label><input id="totalEpisodes"
                    class="form-control" name="totalEpisodes" type="number" min="1" value="${movie.totalEpisodes}" />
            </div>
            <div>
                <label class="form-label" for="rentalPrice">Giá thuê</label><input id="rentalPrice" class="form-control"
                    name="rentalPrice" type="number" min="0" step="0.01" value="${movie.rentalPrice}" />
            </div>
            <div class="full-width d-flex flex-wrap gap-4">
                <label class="form-check"><input class="form-check-input" type="checkbox" name="isVipOnly" value="true"
                        ${movie.vipOnly ? 'checked' : '' } /><span class="form-check-label">Phim dành cho
                        VIP</span></label><label class="form-check"><input class="form-check-input" type="checkbox"
                        name="isRentOnly" value="true" ${movie.rentOnly ? 'checked' : '' } /><span
                        class="form-check-label">Phim thuê lẻ TVOD</span></label>
            </div>
            <div class="full-width d-flex gap-2">
                <button class="btn btn-accent" type="submit">Lưu phim</button><a class="btn btn-noir-outline"
                    href="${pageContext.request.contextPath}/admin/movies">Hủy</a>
            </div>
        </form>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/form-validation.js" defer></script>
</body>

</html>
