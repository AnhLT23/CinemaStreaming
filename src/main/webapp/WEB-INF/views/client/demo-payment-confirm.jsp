<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Ngân hàng demo | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section">
        <section class="payment-card demo-bank-card">
            <span class="eyebrow">MÔ PHỎNG NGÂN HÀNG · KHÔNG GIAO DỊCH THẬT</span>
            <h1>TVHAY Demo Bank</h1>
            <p>
                Đây là màn hình xác nhận giả lập để trình diễn luồng thanh toán. Không
                có tiền thật được chuyển.
            </p>
            <dl class="payment-details">
                <dt>Mã đơn</dt>
                <dd>
                    <code>
                        <c:out value="${transaction.transactionCode}" />
                    </code>
                </dd>
                <dt>Số tiền demo</dt>
                <dd>
                    <strong>
                        <fmt:formatNumber value="${transaction.amount}" type="number" groupingUsed="true"
                            maxFractionDigits="0" />
                        ₫
                    </strong>
                </dd>
                <dt>Nội dung</dt>
                <dd>
                    <c:out value="${transaction.transferContent}" />
                </dd>
                <dt>Trạng thái</dt>
                <dd>
                    <c:out value="${transaction.paymentStatus}" />
                </dd>
            </dl>
            <c:choose>
                <c:when test="${transaction.paymentStatus eq 'SUCCESS'}">
                    <p class="demo-payment-success">
                        Thanh toán demo thành công. Quyền xem đã được kích hoạt.
                    </p>
                    <a class="btn btn-accent" href="${fn:escapeXml(watchUrl)}">Tiếp tục xem</a>
                </c:when>
                <c:when test="${transaction.paymentStatus eq 'PENDING'}">
                    <form action="${pageContext.request.contextPath}/demo-payment" method="post">
                        <input type="hidden" name="csrfToken"
                            value="${fn:escapeXml(sessionScope.csrfToken)}" />
                        <input type="hidden" name="code"
                            value="${fn:escapeXml(transaction.transactionCode)}" />
                        <button class="btn btn-accent" type="submit">
                            Xác nhận thanh toán demo
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p>Giao dịch này đã kết thúc và không thể xác nhận.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
</body>

</html>
