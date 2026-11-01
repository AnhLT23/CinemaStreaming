<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Gói thành viên | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />
        <div class="center-heading">
            <span class="eyebrow">NÂNG TẦM TRẢI NGHIỆM</span>
            <h1>Chọn gói phù hợp</h1>
            <p>Thưởng thức phim chất lượng cao, không gián đoạn.</p>
        </div>
        <div class="row g-4 justify-content-center">
            <c:forEach items="${plans}" var="plan">
                <div class="col-md-6 col-lg-4">
                    <article class="plan-card">
                        <span class="eyebrow">TVHAY+ VIP</span>
                        <h2>
                            <c:out value="${plan.planName}" />
                        </h2>
                        <p>
                            <c:out value="${plan.description}" />
                        </p>
                        <div class="plan-price">
                            <strong>
                                <fmt:formatNumber value="${plan.price}" type="number" groupingUsed="true" />
                            </strong>
                            <span>₫ /
                                <c:out value="${plan.durationDays}" /> ngày
                            </span>
                        </div>
                        <ul>
                            <li>Phim VIP độc quyền</li>
                            <li>Chất lượng tối đa
                                <c:out value="${plan.maxQuality}" />
                            </li>
                            <li>
                                <c:out value="${plan.maxScreens}" /> thiết bị xem đồng thời
                            </li>
                        </ul>
                        <form action="${pageContext.request.contextPath}/subscription" method="post">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                            <input type="hidden" name="action" value="buyPlan" />
                            <input type="hidden" name="planId" value="${plan.planId}" />
                            <label class="form-label" for="planPayment${plan.planId}">Phương thức thanh toán</label>
                            <select class="form-select mb-3" id="planPayment${plan.planId}" name="paymentMethod"
                                required>
                                <option value="DEMO">
                                    Ngân hàng ảo (Demo, không chuyển tiền thật)
                                </option>
                                <option value="VIETQR">Chuyển khoản QR</option>
                            </select>
                            <button class="btn btn-accent w-100" type="submit">
                                Chọn gói
                            </button>
                        </form>
                    </article>
                </div>
            </c:forEach>
            <c:if test="${empty plans}">
                <div class="col-12">
                    <p class="empty-state">
                        Hiện chưa có gói thành viên nào. Vui lòng quay lại sau.
                    </p>
                </div>
            </c:if>
        </div>
        <section class="mt-5">
            <div class="section-heading">
                <div>
                    <span class="eyebrow">THUÊ LẺ TVOD</span>
                    <h2>Phim chiếu rạp</h2>
                </div>
            </div>
            <div class="row row-cols-2 row-cols-sm-3 row-cols-lg-5 g-3">
                <c:forEach items="${theatricalMovies}" var="movie">
                    <c:set var="cardMovie" value="${movie}" scope="request" />
                    <div class="col">
                        <jsp:include page="/WEB-INF/include/movie-card.jsp" />
                        <form action="${pageContext.request.contextPath}/subscription" method="post" class="mt-2">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                            <input type="hidden" name="action" value="rentMovie" />
                            <input type="hidden" name="movieId" value="${movie.movieId}" />
                            <label class="visually-hidden" for="rentPayment${movie.movieId}">Phương thức thanh
                                toán</label>
                            <select class="form-select form-select-sm mb-2" id="rentPayment${movie.movieId}"
                                name="paymentMethod">
                                <option value="DEMO">Ngân hàng ảo (Demo)</option>
                                <option value="VIETQR">Chuyển khoản QR</option>
                            </select>
                            <button class="btn btn-noir-outline btn-sm w-100" type="submit">
                                Thuê 48 giờ ·
                                <fmt:formatNumber value="${movie.rentalPrice}" type="number" groupingUsed="true" />
                                ₫
                            </button>
                        </form>
                    </div>
                </c:forEach>
                <c:if test="${empty theatricalMovies}">
                    <div class="col-12">
                        <p class="empty-state">
                            Hiện chưa có phim cho thuê. Vui lòng quay lại sau.
                        </p>
                    </div>
                </c:if>
            </div>
        </section>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
