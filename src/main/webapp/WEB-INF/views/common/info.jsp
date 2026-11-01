<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme}">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>
        <c:out value="${pageTitle}" /> | TVHAY+
    </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
    <style>
        .info-page {
            max-width: 1000px;
            min-height: 55vh;
            margin: 0 auto;
            padding: 48px 20px;
        }

        .info-panel {
            padding: clamp(22px, 4vw, 40px);
            border: 1px solid var(--noir-border);
            border-radius: 18px;
            background: var(--surface-low);
        }

        .info-panel h1 {
            margin-bottom: 20px;
            color: var(--tvhay-primary, #ffc665);
        }

        .info-panel li {
            margin: 10px 0;
        }

        .info-panel a {
            color: var(--tvhay-primary, #ffc665);
        }

    </style>
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />
    <main class="info-page">
        <article class="info-panel">
            <h1>
                <c:out value="${pageTitle}" />
            </h1>
            <c:choose>
                <c:when test="${pageKey eq 'about'}">
                    <p>
                        TVHAY+ là ứng dụng xem phim mẫu được xây dựng để trình diễn các
                        chức năng khám phá phim, tài khoản và lịch sử xem.
                    </p>
                    <p>
                        Một số nội dung video có thể được cung cấp từ nguồn bên ngoài;
                        chất lượng và khả năng phát phụ thuộc vào nguồn đó.
                    </p>
                    <p>
                        <a href="${pageContext.request.contextPath}/movies">Khám phá danh sách phim</a>
                    </p>
                </c:when>
                <c:when test="${pageKey eq 'terms'}">
                    <p>
                        Đây là ứng dụng demo phục vụ học tập và thử nghiệm. Vui lòng không
                        dùng ứng dụng để thực hiện giao dịch hoặc lưu thông tin thanh toán
                        thật.
                    </p>
                    <p>
                        Tùy chọn “Ngân hàng ảo (Demo)' chỉ mô phỏng xác nhận đơn hàng; nó
                        không chuyển tiền và không đại diện cho ngân hàng hay cổng thanh
                        toán thật.
                    </p>
                    <p>
                        Việc xem video có thể phụ thuộc vào dịch vụ bên ngoài và các nội
                        dung có thể thay đổi theo môi trường triển khai.
                    </p>
                </c:when>
                <c:when test="${pageKey eq 'privacy'}">
                    <p>
                        Ứng dụng có thể lưu thông tin hồ sơ do bạn cung cấp, trạng thái
                        phiên đăng nhập, lịch sử xem và các giao dịch demo trong cơ sở dữ
                        liệu của môi trường triển khai.
                    </p>
                    <p>
                        Không nhập mật khẩu đang dùng ở dịch vụ khác hoặc thông tin thanh
                        toán thật. Dữ liệu demo có thể được quản trị viên môi trường xóa
                        hoặc đặt lại.
                    </p>
                    <p>
                        <a href="${pageContext.request.contextPath}/register">Tạo tài khoản</a>
                        hoặc
                        <a href="${pageContext.request.contextPath}/login">đăng nhập</a>.
                    </p>
                </c:when>
                <c:when test="${pageKey eq 'faq'}">
                    <h2>Làm thế nào để đăng nhập?</h2>
                    <p>
                        Dùng tài khoản đã đăng ký hoặc chọn một tài khoản demo tại trang
                        <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>.
                    </p>
                    <h2>Làm thế nào để xem thông tin tài khoản?</h2>
                    <p>
                        Đăng nhập rồi chọn “Tài khoản của tôi' ở đầu trang. Tại đây có hồ
                        sơ, phim thuê và lịch sử xem.
                    </p>
                    <h2>Thanh toán QR có dùng tiền thật không?</h2>
                    <p>
                        Không. Chọn phương thức ngân hàng ảo để thử luồng demo; không quét
                        hoặc chuyển tiền thật.
                    </p>
                </c:when>
                <c:when test="${pageKey eq 'sitemap'}">
                    <p>Các trang và chức năng chính:</p>
                    <ul>
                        <li>
                            <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/movies">Danh sách phim</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/subscription">Gói VIP và thuê phim</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/account">Tài khoản (yêu cầu đăng nhập)</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/about">Giới thiệu</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/terms">Điều khoản sử dụng</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/privacy">Quyền riêng tư</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/faq">Câu hỏi thường gặp</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/contact">Liên hệ</a>
                        </li>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>
                        Đây là dự án demo và hiện chưa cấu hình hộp thư hỗ trợ hoặc quảng
                        cáo. Vui lòng liên hệ người quản lý môi trường triển khai để được
                        hỗ trợ.
                    </p>
                </c:otherwise>
            </c:choose>
        </article>
    </main>
    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
</body>

</html>
