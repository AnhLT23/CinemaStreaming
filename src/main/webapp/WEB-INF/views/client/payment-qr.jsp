<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Thanh toán | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="container content-section">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />
        <div class="payment-card">
            <span class="eyebrow">
                <c:choose>
                    <c:when test="${demoPayment}">NGÂN HÀNG ẢO · DEMO</c:when>
                    <c:otherwise>THANH TOÁN ĐƠN HÀNG</c:otherwise>
                </c:choose>
            </span>
            <h1>
                <c:choose>
                    <c:when test="${demoPayment}">Quét mã để mở xác nhận demo</c:when>
                    <c:otherwise>Quét mã QR để thanh toán</c:otherwise>
                </c:choose>
            </h1>
            <p>
                Đơn hàng
                <strong>
                    <c:out value="${transaction.transactionCode}" />
                </strong>
            </p>
            <c:choose>
                <c:when test="${not empty qrImageUrl}">
                    <img class="payment-qr" src="${fn:escapeXml(qrImageUrl)}" alt="Mã QR thanh toán" />
                </c:when>
                <c:otherwise>
                    <div class="qr-placeholder" role="img" aria-label="Mã QR thanh toán chưa khả dụng">
                        QR
                    </div>
                </c:otherwise>
            </c:choose>
            <p>
                <strong>
                    <fmt:formatNumber value="${transaction.amount}" type="number" groupingUsed="true"
                        maxFractionDigits="0" />
                    ₫
                </strong>
            </p>
            <dl class="payment-details">
                <dt>Ngân hàng</dt>
                <dd>
                    <c:out value="${bankName}" />
                </dd>
                <dt>Số tài khoản</dt>
                <dd>
                    <c:out value="${bankAccount}" />
                </dd>
                <dt>Nội dung chuyển khoản</dt>
                <dd>
                    <code>
                        <c:out value="${transaction.transferContent}" />
                    </code>
                </dd>
                <dt>Trạng thái</dt>
                <dd id="paymentStatus">
                    <c:out value="${transaction.paymentStatus}" />
                </dd>
            </dl>
            <c:choose>
                <c:when test="${demoPayment}">
                    <p class="demo-payment-note">
                        Đây là ngân hàng giả lập, không chuyển tiền thật. Quét QR để mở
                        màn hình ngân hàng demo, sau đó bấm xác nhận.
                    </p>
                    <a class="btn btn-accent"
                        href="${pageContext.request.contextPath}/demo-payment?code=${fn:escapeXml(transaction.transactionCode)}">Mở
                        ngân hàng demo</a>
                </c:when>
                <c:otherwise>
                    <p>
                        Thời gian thanh toán còn:
                        <strong id="paymentCountdown"
                            data-seconds="${fn:escapeXml(countdownSeconds)}"></strong>
                    </p>
                    <p class="text-secondary">
                        Vui lòng giữ nguyên nội dung chuyển khoản. Trạng thái sẽ cập nhật
                        sau khi cổng thanh toán xác nhận giao dịch.
                    </p>
                </c:otherwise>
            </c:choose>
            <a class="btn btn-noir-outline" href="${pageContext.request.contextPath}/account">Về tài khoản</a>
            <span id="paymentConfig" hidden
                data-status-url="${pageContext.request.contextPath}/subscription?action=status&amp;code=${fn:escapeXml(transaction.transactionCode)}"
                data-success-url="${fn:escapeXml(successRedirect)}"></span>
        </div>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script>
        (function() {
            const config = document.getElementById("paymentConfig");
            const statusElement = document.getElementById("paymentStatus");
            const countdownElement = document.getElementById("paymentCountdown");
            if (countdownElement) {
                let seconds = Number(countdownElement.dataset.seconds) || 900;
                const renderCountdown = function() {
                    const minutes = Math.floor(seconds / 60);
                    const remainingSeconds = seconds % 60;
                    countdownElement.textContent =
                        minutes + ":" + String(remainingSeconds).padStart(2, "0");
                    if (seconds > 0) seconds -= 1;
                };
                renderCountdown();
                window.setInterval(renderCountdown, 1000);
            }
            window.setInterval(async function() {
                try {
                    const response = await fetch(config.dataset.statusUrl, {
                        credentials: "same-origin",
                    });
                    if (!response.ok) return;
                    const payment = await response.json();
                    statusElement.textContent = payment.status;
                    if (payment.status === "SUCCESS") {
                        window.location.assign(config.dataset.successUrl);
                    } else if (payment.status === "FAILED") {
                        statusElement.textContent = "Giao dịch chưa thành công";
                    }
                } catch (error) {
                    statusElement.textContent = "Đang chờ cập nhật";
                }
            }, 3000);
        })();

    </script>
</body>

</html>
