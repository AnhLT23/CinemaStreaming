<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Hỗ trợ vé thuê | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />
        <c:if test="${not empty flashOk}">
            <div class="alert alert-success mt-4" role="status">
                <c:out value="${flashOk}" />
            </div>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="alert alert-danger mt-4" role="alert">
                <c:out value="${flashError}" />
            </div>
        </c:if>
        <c:if test="${authRequired}">
            <div class="alert alert-info mt-4" role="status">
                Đây là giao diện xem trước. Đăng nhập bằng tài khoản
                Staff/Admin để tra cứu khách hàng và quản lý vé.
                <a href="${pageContext.request.contextPath}/login" class="alert-link">Đăng nhập</a>
            </div>
        </c:if>
        <div class="section-heading">
            <div>
                <span class="eyebrow">STAFF SUPPORT</span>
                <h1>Hỗ trợ vé thuê phim</h1>
            </div>
        </div>
        <section class="panel support-search">
            <h2 class="h4">Tra cứu khách hàng</h2>
            <p class="text-secondary">
                Nhập mã hội viên để xem vé thuê, thời hạn và lịch sử thu
                hồi.
            </p>
            <form action="${pageContext.request.contextPath}/staff/rentals" method="get" class="row g-2">
                <div class="col-md-8">
                    <label class="visually-hidden" for="customerCode">Mã khách hàng</label>
                    <input class="form-control" id="customerCode" name="code" value="${fn:escapeXml(code)}"
                        placeholder="Ví dụ: #TVH-XXXXX" maxlength="20" required <c:if test="${authRequired}">disabled
                    </c:if>
                    />
                </div>
                <div class="col-md-4">
                    <button class="btn btn-accent w-100" type="submit" <c:if test="${authRequired}">disabled</c:if>
                        >
                        Tra cứu vé
                    </button>
                </div>
            </form>
        </section>

        <c:if test="${not empty customerCode}">
            <section class="panel mt-4">
                <span class="eyebrow">THÔNG TIN ĐÃ CHE</span>
                <h2 class="h3 mt-2">
                    <c:out value="${customerName}" />
                </h2>
                <dl class="rental-details">
                    <dt>Mã khách hàng</dt>
                    <dd>
                        <c:out value="${customerCode}" />
                    </dd>
                    <dt>Email</dt>
                    <dd>
                        <c:out value="${maskedEmail}" />
                    </dd>
                    <dt>Số điện thoại</dt>
                    <dd>
                        <c:out value="${maskedPhone}" />
                    </dd>
                </dl>
                <p class="text-secondary mb-0">
                    Tổng số vé thuê:
                    <strong>
                        <c:out value="${rentals.size()}" />
                    </strong>
                </p>
            </section>
            <section class="panel mt-4">
                <h2 class="h4">Danh sách vé thuê</h2>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr>
                                <th>Mã vé</th>
                                <th>Phim</th>
                                <th>Mã giao dịch</th>
                                <th>Thuê lúc</th>
                                <th>Hết hạn</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${rentals}" var="rental">
                                <tr>
                                    <td>
                                        #
                                        <c:out value="${rental.rentalId}" />
                                    </td>
                                    <td>
                                        <c:out value="${rental.movieTitle}" />
                                    </td>
                                    <td>
                                        <c:out value="${rental.transactionCode}" />
                                    </td>
                                    <td>
                                        <c:out value="${rental.rentedAt}" />
                                    </td>
                                    <td>
                                        <c:out value="${rental.expiresAt}" />
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${rental.status == 'ACTIVE'}">
                                                <span class="badge text-bg-success">Đang hoạt động</span>
                                            </c:when>
                                            <c:when test="${rental.status == 'EXPIRED'}">
                                                <span class="badge text-bg-secondary">Đã hết hạn</span>
                                            </c:when>
                                            <c:when test="${rental.status == 'REVOKED'}">
                                                <span class="badge text-bg-danger">Đã thu hồi</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-pill">
                                                    <c:out value="${rental.status}" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${rental.status == 'ACTIVE'}">
                                            <button class="btn btn-outline-danger btn-sm" type="button"
                                                data-bs-toggle="modal" data-bs-target="#revokeModal"
                                                data-rental-id="${fn:escapeXml(rental.rentalId)}"
                                                data-movie-title="${fn:escapeXml(rental.movieTitle)}">
                                                Thu hồi
                                            </button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty rentals}">
                                <tr>
                                    <td colspan="7" class="text-secondary text-center py-4">
                                        Khách hàng chưa có vé thuê phim.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="panel mt-4">
                <h2 class="h4">Lịch sử thu hồi vé</h2>
                <p class="text-secondary">
                    Nhật ký kiểm toán được ghi cùng giao dịch thu hồi và
                    không thể sửa từ màn hình này.
                </p>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr>
                                <th>Mã nhật ký</th>
                                <th>Mã vé</th>
                                <th>Nhân viên</th>
                                <th>Lý do</th>
                                <th>Thời điểm</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${revokeLogs}" var="log">
                                <tr>
                                    <td>
                                        #
                                        <c:out value="${log.logId}" />
                                    </td>
                                    <td>
                                        #
                                        <c:out value="${log.rentalId}" />
                                    </td>
                                    <td>
                                        User #
                                        <c:out value="${log.performedBy}" />
                                    </td>
                                    <td>
                                        <c:out value="${log.revokeReason}" />
                                    </td>
                                    <td>
                                        <c:out value="${log.revokedAt}" />
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty revokeLogs}">
                                <tr>
                                    <td colspan="5" class="text-secondary text-center py-4">
                                        Chưa có nhật ký thu hồi.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <div class="modal fade" id="revokeModal" tabindex="-1" aria-labelledby="revokeModalLabel"
                aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <form action="${pageContext.request.contextPath}/staff/rentals" method="post">
                            <div class="modal-header">
                                <h2 class="modal-title fs-5" id="revokeModalLabel">
                                    Thu hồi vé thuê
                                </h2>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="Đóng"></button>
                            </div>
                            <div class="modal-body">
                                <input type="hidden" name="action" value="revoke" />
                                <input type="hidden" name="csrfToken"
                                    value="${fn:escapeXml(sessionScope.csrfToken)}" />
                                <input type="hidden" name="code" value="${fn:escapeXml(customerCode)}" />
                                <input type="hidden" name="rentalId" id="revokeRentalId" />
                                <p>
                                    Vé phim:
                                    <strong id="revokeMovieTitle"></strong>
                                </p>
                                <label class="form-label" for="revokeReason">Lý do thu hồi vé *</label>
                                <textarea class="form-control" id="revokeReason" name="reason" rows="4" maxlength="255"
                                    required></textarea>
                                <div class="form-text">
                                    Nhập lý do thu hồi, tối đa 255 ký tự. Lý
                                    do sẽ được lưu trong nhật ký kiểm toán.
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-noir-outline" data-bs-dismiss="modal">
                                    Hủy
                                </button>
                                <button type="submit" class="btn btn-danger">
                                    Xác nhận thu hồi
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:if>
        <c:if test="${not empty searchError}">
            <div class="alert alert-warning mt-4" role="status">
                <c:out value="${searchError}" />
            </div>
        </c:if>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script>
        document
            .getElementById("revokeModal")
            ?.addEventListener("show.bs.modal", function(event) {
                const button = event.relatedTarget;
                document.getElementById("revokeRentalId").value =
                    button.dataset.rentalId;
                document.getElementById("revokeMovieTitle").textContent =
                    button.dataset.movieTitle;
                document.getElementById("revokeReason").value = "";
            });

    </script>
</body>

</html>
