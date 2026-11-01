<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý banner | TVHAY+</title>
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
                <h1>Banner trang chủ</h1>
            </div>
        </div>
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin">Tổng quan</a><a
                href="${pageContext.request.contextPath}/admin/movies">Phim</a><a
                href="${pageContext.request.contextPath}/admin/episodes">Tập phim</a><a class="active"
                href="${pageContext.request.contextPath}/admin/banners">Banner</a><a
                href="${pageContext.request.contextPath}/admin/comments">Bình luận</a><a
                href="${pageContext.request.contextPath}/admin/members">Hội viên</a>
        </nav>
        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <c:out value="${error}" />
            </div>
        </c:if>
        <fmt:formatDate value="${editBanner.startDate}" pattern="yyyy-MM-dd'T'HH:mm" var="startDateValue" />
        <fmt:formatDate value="${editBanner.endDate}" pattern="yyyy-MM-dd'T'HH:mm" var="endDateValue" />
        <c:choose>
            <c:when test="${fn:startsWith(editBanner.imageUrl, 'http')}">
                <c:set var="editImageUrl" value="${editBanner.imageUrl}" />
            </c:when>
            <c:when test="${fn:startsWith(editBanner.imageUrl, 'assets/img/')}">
                <c:url var="editImageUrl" value="/${editBanner.imageUrl}" />
            </c:when>
            <c:otherwise>
                <c:url var="editImageUrl" value="/image/${editBanner.imageUrl}" />
            </c:otherwise>
        </c:choose>
        <section class="panel mb-4">
            <h2 class="h4">
                <c:choose>
                    <c:when test="${editBanner.bannerId gt 0}">Cập nhật banner</c:when>
                    <c:otherwise>Tạo banner</c:otherwise>
                </c:choose>
            </h2>
            <form action="${pageContext.request.contextPath}/admin/banners" method="post" enctype="multipart/form-data"
                class="form-grid">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input type="hidden"
                    name="action" value="save" /><input type="hidden" name="bannerId" value="${editBanner.bannerId}" />
                <div>
                    <label class="form-label" for="bannerTitle">Tiêu đề</label><input id="bannerTitle"
                        class="form-control" name="title" value="${fn:escapeXml(editBanner.title)}" required
                        maxlength="200" />
                </div>
                <div>
                    <label class="form-label" for="targetUrl">Đường dẫn đích</label><input id="targetUrl"
                        class="form-control" name="targetUrl" value="${fn:escapeXml(editBanner.targetUrl)}" required
                        maxlength="500" placeholder="/movie/1 hoặc https://example.com" />
                </div>
                <div>
                    <label class="form-label" for="position">Vị trí</label><input id="position" class="form-control"
                        name="position" value="${fn:escapeXml(editBanner.position)}" maxlength="50" required />
                </div>
                <div>
                    <label class="form-label" for="movieId">Movie ID (không bắt buộc)</label><input id="movieId"
                        class="form-control" name="movieId" type="number" min="1" value="${editBanner.movieId}" />
                </div>
                <div>
                    <label class="form-label" for="startDate">Bắt đầu hiển thị</label><input id="startDate"
                        class="form-control" type="datetime-local" name="startDate" value="${startDateValue}"
                        required />
                </div>
                <div>
                    <label class="form-label" for="endDate">Kết thúc hiển thị</label><input id="endDate"
                        class="form-control" type="datetime-local" name="endDate" value="${endDateValue}" required />
                </div>
                <div class="full-width">
                    <label class="form-label" for="imageFile">Ảnh banner (JPG, PNG hoặc WEBP)</label><input
                        id="imageFile" class="form-control" type="file" name="imageFile" accept=".jpg,.jpeg,.png,.webp"
                        ${editBanner.bannerId gt 0 ? '' : 'required' } />
                    <c:if test="${not empty editBanner.imageUrl}"><img class="mt-2" src="${editImageUrl}"
                            alt="Ảnh banner hiện tại" style="max-width: 240px" /></c:if>
                </div>
                <div class="full-width">
                    <label class="form-check"><input class="form-check-input" type="checkbox" name="isActive"
                            value="true" ${editBanner.active ? 'checked' : '' } /><span class="form-check-label">Đang
                            bật</span></label><button class="btn btn-accent mt-3" type="submit">
                        Lưu banner
                    </button>
                </div>
            </form>
        </section>
        <div class="row g-3">
            <c:forEach items="${banners}" var="item">
                <c:choose>
                    <c:when test="${fn:startsWith(item.imageUrl, 'http')}">
                        <c:set var="itemImageUrl" value="${item.imageUrl}" />
                    </c:when>
                    <c:when test="${fn:startsWith(item.imageUrl, 'assets/img/')}">
                        <c:url var="itemImageUrl" value="/${item.imageUrl}" />
                    </c:when>
                    <c:otherwise>
                        <c:url var="itemImageUrl" value="/image/${item.imageUrl}" />
                    </c:otherwise>
                </c:choose>
                <div class="col-md-6">
                    <article class="banner-admin-card">
                        <img src="${itemImageUrl}" alt="" />
                        <div>
                            <strong>
                                <c:out value="${item.title}" />
                            </strong>
                            <p>${item.startDate} — ${item.endDate}</p>
                            <span class="status-pill">${item.active ? 'Đang bật' : 'Đã
                                tắt'}</span>
                            <p>
                                <a class="btn btn-sm btn-noir-outline"
                                    href="${pageContext.request.contextPath}/admin/banners?edit=${item.bannerId}">Sửa</a>
                            </p>
                            <form action="${pageContext.request.contextPath}/admin/banners" method="post" class="mt-2">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input
                                    type="hidden" name="action" value="delete" /><input type="hidden" name="bannerId"
                                    value="${item.bannerId}" /><button class="btn btn-sm btn-outline-danger"
                                    type="submit">
                                    Xóa banner
                                </button>
                            </form>
                        </div>
                    </article>
                </div>
            </c:forEach>
        </div>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
