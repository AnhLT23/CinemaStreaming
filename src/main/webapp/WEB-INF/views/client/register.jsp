<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="csrf-token" content="${sessionScope.csrfToken}" />
    <title>Đăng ký | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sign-in.css" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet" />
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20,400,0,0" rel="stylesheet" />
</head>
<body class="login-page auth-page auth-page--register">
    <main class="auth-card" aria-labelledby="auth-title">
        <a class="auth-brand" href="${pageContext.request.contextPath}/home" aria-label="TVHAY+ trang chủ">
            TV<span>HAY</span><sup>+</sup>
        </a>
        <h1 id="auth-title">Tạo tài khoản</h1>
        <p class="auth-intro">Đăng ký để khám phá phim mới mỗi ngày.</p>

        <nav class="auth-tabs" aria-label="Tài khoản">
            <a class="auth-tab" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            <a class="auth-tab auth-tab--active" href="${pageContext.request.contextPath}/register" aria-current="page">Đăng ký</a>
        </nav>

        <c:if test="${not empty error}">
            <div class="auth-alert" role="alert"><c:out value="${error}" /></div>
        </c:if>

        <form class="auth-form" action="${pageContext.request.contextPath}/register" method="post" id="register-form">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />

            <div class="auth-field">
                <label for="register-username">Tên đăng nhập</label>
                <input id="register-username" name="username" type="text" value="${fn:escapeXml(username)}"
                       autocomplete="username" minlength="3" maxlength="50" pattern="[A-Za-z0-9_]+"
                       placeholder="Từ 3 đến 50 ký tự" required />
            </div>

            <div class="auth-field">
                <label for="register-fullname">Họ và tên</label>
                <input id="register-fullname" name="fullName" type="text" value="${fn:escapeXml(fullName)}"
                       autocomplete="name" maxlength="100" placeholder="Nhập họ và tên" required />
            </div>

            <div class="auth-field">
                <label for="register-email">Email</label>
                <input id="register-email" name="email" type="email" value="${fn:escapeXml(email)}"
                       autocomplete="email" maxlength="100" placeholder="name@example.com" required />
            </div>

            <div class="auth-field">
                <label for="register-password">Mật khẩu <span>(10–128 ký tự)</span></label>
                <div class="auth-password">
                    <input id="register-password" name="password" type="password" autocomplete="new-password"
                           minlength="10" maxlength="128" placeholder="Tạo mật khẩu" required />
                    <button class="auth-password-toggle" type="button" data-password-target="register-password"
                            aria-label="Hiện mật khẩu">
                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                    </button>
                </div>
            </div>

            <div class="auth-field">
                <label for="register-confirm-password">Nhập lại mật khẩu</label>
                <div class="auth-password">
                    <input id="register-confirm-password" name="confirmPassword" type="password"
                           autocomplete="new-password" maxlength="128" placeholder="Nhập lại mật khẩu" required />
                    <button class="auth-password-toggle" type="button" data-password-target="register-confirm-password"
                            aria-label="Hiện mật khẩu">
                        <span class="material-symbols-outlined" aria-hidden="true">visibility</span>
                    </button>
                </div>
            </div>

            <p id="form-message" class="auth-alert" role="alert" aria-live="polite" hidden></p>
            <button class="auth-submit" type="submit">Tạo tài khoản</button>
        </form>

        <p class="auth-switch">Đã có tài khoản?
            <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
        </p>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/auth-toggle.js" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/form-validation.js" defer></script>
</body>
</html>
