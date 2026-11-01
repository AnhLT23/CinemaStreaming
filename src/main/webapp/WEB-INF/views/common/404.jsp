<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Không tìm thấy trang | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
</head>

<body class="error-body">
    <main class="error-page error-page--not-found">
        <section class="error-card" aria-labelledby="error-title">
            <div class="error-illustration" aria-hidden="true">
                <svg class="error-illustration-icon" viewBox="0 0 48 48" fill="none">
                    <path d="M7 16h34v24H7zM7 16l5-9h9l-5 9m2-9h9l-5 9m2-9h9l-5 9m2-9h9l-5 9" stroke="currentColor"
                        stroke-width="3" stroke-linejoin="round" />
                    <path d="m21 23 10 6-10 6z" fill="currentColor" />
                </svg>
                <span class="error-illustration__spark error-illustration__spark--one"></span>
                <span class="error-illustration__spark error-illustration__spark--two"></span>
            </div>
            <p class="error-eyebrow">TVHAY+ · RẠP PHIM CỦA BẠN</p>
            <h1 id="error-title">Không tìm thấy trang</h1>
            <p class="error-description">
                Có vẻ như đường dẫn này đã đổi chỗ hoặc không còn tồn tại.
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
