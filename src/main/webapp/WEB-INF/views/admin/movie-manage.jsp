<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý phim | TVHAY+</title>
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
                <h1>Kho phim</h1>
            </div>
            <a class="btn btn-accent" href="${pageContext.request.contextPath}/admin/movies?action=create">＋ Thêm
                phim</a>
        </div>
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin">Tổng quan</a><a class="active"
                href="${pageContext.request.contextPath}/admin/movies">Phim</a><a
                href="${pageContext.request.contextPath}/admin/episodes">Tập phim</a><a
                href="${pageContext.request.contextPath}/admin/banners">Banner</a><a
                href="${pageContext.request.contextPath}/admin/comments">Bình luận</a><a
                href="${pageContext.request.contextPath}/admin/members">Hội viên</a>
        </nav>
        <form class="filter-bar row g-2 mb-4" method="get">
            <div class="col">
                <input class="form-control" name="q" value="${param.q}" placeholder="Tìm theo tên phim" />
            </div>
            <div class="col-auto">
                <select class="form-select" name="status">
                    <option value="" ${empty param.status ? 'selected' : '' }>
                        Mọi trạng thái
                    </option>
                    <option value="PUBLISHED" ${param.status eq 'PUBLISHED' ? 'selected' : '' }>
                        Đang hiển thị
                    </option>
                    <option value="DELETED" ${param.status eq 'DELETED' ? 'selected' : '' }>
                        Đã xóa mềm
                    </option>
                </select>
            </div>
            <div class="col-auto">
                <button class="btn btn-noir-outline">Tìm kiếm</button>
            </div>
        </form>
        <div class="table-responsive panel">
            <table class="table noir-table align-middle">
                <thead>
                    <tr>
                        <th>Phim</th>
                        <th>Năm</th>
                        <th>Phân loại</th>
                        <th>Trạng thái</th>
                        <th>Cập nhật</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${movies}" var="movie">
                        <tr>
                            <td>
                                <strong>
                                    <c:out value="${movie.title}" />
                                </strong><small class="d-block text-secondary">
                                    <c:out value="${movie.originalTitle}" />
                                </small>
                            </td>
                            <td>${movie.releaseYear}</td>
                            <td>
                                ${movie.vipOnly ? 'VIP' : 'Miễn phí'} / ${movie.rentOnly ?
                                'TVOD' : 'SVOD'}
                            </td>
                            <td>
                                <span class="status-pill">${movie.deleted ? 'Đã xóa' : 'Đang hiển thị'}</span>
                            </td>
                            <td>${movie.updatedAt}</td>
                            <td class="text-nowrap">
                                <c:if test="${not movie.deleted}"><a class="btn btn-sm btn-noir-outline"
                                        href="${pageContext.request.contextPath}/admin/movies?action=edit&amp;id=${movie.id}">Sửa</a>
                                    <form class="d-inline" action="${pageContext.request.contextPath}/admin/movies"
                                        method="post" onsubmit="return confirm('Ẩn phim này khỏi danh mục?')">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input
                                            type="hidden" name="action" value="delete" /><input type="hidden"
                                            name="movieId" value="${movie.id}" /><button
                                            class="btn btn-sm btn-outline-danger" type="submit">
                                            Xóa mềm
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <c:if test="${empty movies}">
                <p class="empty-state">Chưa có dữ liệu phim.</p>
            </c:if>
        </div>
        <jsp:include page="/WEB-INF/include/pagination.jsp" />
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
