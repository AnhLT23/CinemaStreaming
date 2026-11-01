<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Không có quyền truy cập | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body class="error-body">
    <main class="error-page error-page--forbidden">
        <section class="error-card error-card--forbidden" aria-labelledby="error-title">
            <div class="forbidden-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                    <rect x="4" y="10" width="16" height="11" rx="2" stroke="currentColor" stroke-width="2" />
                    <path d="M8 10V7a4 4 0 1 1 8 0v3m-4 4v3" stroke="currentColor" stroke-width="2"
                        stroke-linecap="round" />
                </svg>
            </div>
            <p class="error-eyebrow">KHU VỰC GIỚI HẠN</p>
            <h1 id="error-title">Bạn chưa có quyền truy cập</h1>
            <p class="error-description">
                Trang này chỉ dành cho tài khoản được cấp quyền. Hãy quay lại khu vực
                bạn có thể truy cập.
            </p>
            <a class="error-home-button" href="${pageContext.request.contextPath}/home">
                <svg class="error-home-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-6v-7h-4v7H4a1 1 0 0 1-1-1z" stroke="currentColor"
                        stroke-width="2" stroke-linejoin="round" />
                </svg>
                Về trang chủ
            </a>
        </section>
    </main>
</body>

</html>
