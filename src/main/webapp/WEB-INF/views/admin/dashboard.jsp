<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản trị | TVHAY+</title>
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
                <h1>Tổng quan</h1>
            </div>
            <span class="text-secondary">Cập nhật ${dashboardUpdatedAt}</span>
        </div>
        <nav class="admin-nav">
            <a class="active" href="${pageContext.request.contextPath}/admin">Tổng quan</a><a
                href="${pageContext.request.contextPath}/admin/movies">Phim</a><a
                href="${pageContext.request.contextPath}/admin/episodes">Tập phim</a><a
                href="${pageContext.request.contextPath}/admin/banners">Banner</a><a
                href="${pageContext.request.contextPath}/admin/comments">Bình luận</a><a
                href="${pageContext.request.contextPath}/admin/members">Hội viên</a>
        </nav>
        <section class="kpi-grid">
            <article class="kpi-card">
                <span>Tổng doanh thu</span><strong>
                    <fmt:formatNumber value="${revenue}" type="number" groupingUsed="true" />
                    ₫
                </strong><small>Từ các giao dịch thành công</small>
            </article>
            <article class="kpi-card">
                <span>Hội viên VIP</span><strong>
                    <fmt:formatNumber value="${vipCount}" type="number" groupingUsed="true" />
                </strong><small>Đang hoạt động</small>
            </article>
            <article class="kpi-card">
                <span>Thiết bị trực tuyến</span><strong>${onlineCount}</strong><small>Đang hoạt động gần đây</small>
            </article>
            <article class="kpi-card">
                <span>Tổng số phim</span><strong>${movieCount}</strong><small>Trong thư viện nội dung</small>
            </article>
        </section>
        <section class="panel mt-4">
            <div class="section-heading">
                <div>
                    <span class="eyebrow">HOẠT ĐỘNG MỚI</span>
                    <h2>Giao dịch gần đây</h2>
                </div>
            </div>
            <div class="table-responsive">
                <table class="table noir-table">
                    <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Hội viên</th>
                            <th>Số tiền</th>
                            <th>Loại</th>
                            <th>Trạng thái</th>
                            <th>Thời gian</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${recentTransactions}" var="tx">
                            <tr>
                                <td>
                                    <c:out value="${tx.transactionCode}" />
                                </td>
                                <td>
                                    <c:out value="${tx.username}" />
                                </td>
                                <td>
                                    <fmt:formatNumber value="${tx.amount}" type="number" groupingUsed="true" />
                                    ₫
                                </td>
                                <td>${tx.transactionType}</td>
                                <td>
                                    <span class="status-pill">${tx.status}</span>
                                </td>
                                <td>${tx.createdAt}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
