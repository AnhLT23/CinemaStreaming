<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Kiểm duyệt bình luận | TVHAY+</title>
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
                <h1>Kiểm duyệt bình luận</h1>
            </div>
        </div>
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin">Tổng quan</a><a
                href="${pageContext.request.contextPath}/admin/movies">Phim</a><a
                href="${pageContext.request.contextPath}/admin/episodes">Tập phim</a><a
                href="${pageContext.request.contextPath}/admin/banners">Banner</a><a class="active"
                href="${pageContext.request.contextPath}/admin/comments">Bình luận</a><a
                href="${pageContext.request.contextPath}/admin/members">Hội viên</a>
        </nav>
        <form class="filter-bar row g-2 mb-4" method="get">
            <div class="col-md-5">
                <input class="form-control" name="q" value="${fn:escapeXml(q)}"
                    placeholder="Tìm nội dung / thành viên / phim" />
            </div>
            <div class="col-auto">
                <%-- jspformatselect0token --%>
            </div>
            <div class="col-auto">
                <button class="btn btn-noir-outline">Lọc</button>
            </div>
        </form>
        <div class="comment-moderation">
            <c:forEach items="${comments}" var="comment">
                <article class="panel">
                    <div class="d-flex justify-content-between gap-3">
                        <div>
                            <strong>
                                <c:out value="${comment.username}" />
                            </strong><span class="text-secondary">
                                ·
                                <c:out value="${comment.movieTitle}" />
                            </span>
                            <div class="text-warning">
                                ${comment.rating} ★
                            </div>
                        </div>
                        <span class="status-pill">${comment.visible ? 'Đang hiển thị' : 'Đã
                            ẩn'}</span>
                    </div>
                    <p class="my-3">
                        <c:out value="${comment.content}" />
                    </p>
                    <small class="text-secondary">${comment.createdAt}</small>
                    <form action="${pageContext.request.contextPath}/admin/comments" method="post" class="mt-3">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input type="hidden"
                            name="commentId" value="${comment.commentId}" /><input type="hidden" name="action"
                            value="${comment.visible ? 'hide' : 'show'}" /><button
                            class="btn btn-sm ${comment.visible ? 'btn-outline-danger' : 'btn-accent'}">
                            ${comment.visible ? 'Ẩn bình luận' : 'Hiện bình
                            luận'}
                        </button>
                    </form>
                </article>
            </c:forEach>
            <c:if test="${empty comments}">
                <p class="empty-state">
                    Không có bình luận cần kiểm duyệt.
                </p>
            </c:if>
        </div>
        <jsp:include page="/WEB-INF/include/pagination.jsp" />
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
