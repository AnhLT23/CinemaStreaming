<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="csrf-token" content="${sessionScope.csrfToken}" />
    <title>Đăng nhập | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sign-in.css" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20,400,0,0" rel="stylesheet" />
</head>
<body class="login-page auth-page auth-page--login">
    <main class="auth-card" aria-labelledby="auth-title">
        <a class="auth-brand" href="${pageContext.request.contextPath}/home" aria-label="TVHAY+ trang chủ">
            TV<span>HAY</span><sup>+</sup>
        </a>
        <h1 id="auth-title">Đăng nhập</h1>
        <p class="auth-intro">Đăng nhập để tiếp tục hành trình xem phim của bạn.</p>

        <nav class="auth-tabs" aria-label="Tài khoản">
            <a class="auth-tab auth-tab--active" href="${pageContext.request.contextPath}/login" aria-current="page">Đăng nhập</a>
            <a class="auth-tab" href="${pageContext.request.contextPath}/register">Đăng ký</a>
        </nav>

        <c:if test="${not empty error}">
            <div class="auth-alert" role="alert"><c:out value="${error}" /></div>
        </c:if>

        <form class="auth-form" action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <input type="hidden" name="redirect" value="${fn:escapeXml(redirectPath)}" />

            <div class="auth-field">
                <label for="login-identity">Tên đăng nhập, email hoặc số điện thoại</label>
                <input id="login-identity" type="text" name="login" value="${fn:escapeXml(param.login)}"
                       autocomplete="username" maxlength="254" placeholder="Nhập tên đăng nhập hoặc email" required />
            </div>

            <div class="auth-field">
                <label for="login-password">Mật khẩu</label>
                <div class="auth-password">
                    <input id="login-password" type="password" name="password"
                           autocomplete="current-password" placeholder="Nhập mật khẩu" required />
                    <button class="auth-password-toggle" type="button" data-password-target="login-password"
                            aria-label="Hiện mật khẩu">
                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                    </button>
                </div>
            </div>

            <button class="auth-submit" type="submit">Đăng nhập</button>
        </form>

        <p class="auth-switch">Chưa có tài khoản?
            <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
        </p>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/auth-toggle.js" defer></script>
</body>
</html>
