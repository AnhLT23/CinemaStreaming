<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Khởi tạo theme ngay lập tức để chống chớp màn hình khi chuyển trang --%>
<script>
    (function() {
        var sessionTheme =
            "${not empty sessionScope.theme ? sessionScope.theme : (not empty sessionScope.loggedInUser.theme ? sessionScope.loggedInUser.theme : '')}";
        var saved =
            localStorage.getItem("tvhay_theme") ||
            localStorage.getItem("theme") ||
            sessionTheme ||
            "dark";
        var norm = saved === "light" ? "light" : "dark";
        document.documentElement.setAttribute("data-theme", norm);
        document.documentElement.setAttribute("data-bs-theme", norm);
        document.documentElement.classList.toggle("dark", norm === "dark");
        document.documentElement.classList.toggle("light", norm === "light");
    })();

</script>

<link rel="preconnect" href="https://fonts.googleapis.com" />
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
<link
    href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@600;700;800&family=Inter:wght@400;600;700;800&display=swap"
    rel="stylesheet" />
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0"
    rel="stylesheet" />

<style>
    /* 1. BỘ BIẾN MÀU TỐI MẶC ĐỊNH (DARK CINEMA NOIR) */
    :root,
    html[data-theme="dark"],
    body[data-theme="dark"] {
        --tvhay-bg: #111319;
        --tvhay-surface-lowest: #0c0e14;
        --tvhay-surface-low: #191b22;
        --tvhay-surface: #1d1f26;
        --tvhay-surface-high: #282a30;
        --tvhay-surface-highest: #33353b;
        --tvhay-header-bg: rgba(12, 14, 20, 0.92);
        --tvhay-primary: #ffc665;
        --tvhay-primary-container: #e5a93c;
        --tvhay-on-primary: #432c00;
        --tvhay-secondary-container: #e10111;
        --tvhay-text: #e2e2ea;
        --tvhay-text-muted: #d4c4b0;
        --tvhay-outline: #9d8f7c;
        --tvhay-border: rgba(255, 255, 255, 0.08);
        --tvhay-card-shadow: 0 4px 12px rgba(0, 0, 0, 0.45);
        /* Tương thích với biến trong cinema-noir.css và footer.jsp */
        --noir-bg: #101114;
        --noir-text: #f4f3f0;
        --noir-muted: #a0a2aa;
        --noir-border: #30333b;
        --canvas: #111319;
        --surface-low: #191b22;
        --surface-mid: #1d1f26;
        --surface-high: #282a30;
        --surface-stroke: #33353b;
        --text-white: #e2e2ea;
        --text-primary: #e2e2ea;
    }

    /* 2. BỘ BIẾN MÀU SÁNG (LIGHT MODE) */
    html[data-theme="light"],
    body[data-theme="light"] {
        --tvhay-bg: #f4f6f9;
        --tvhay-surface-lowest: #ffffff;
        --tvhay-surface-low: #ffffff;
        --tvhay-surface: #eaeff5;
        --tvhay-surface-high: #dde3ea;
        --tvhay-surface-highest: #cbd5e1;
        --tvhay-header-bg: rgba(255, 255, 255, 0.94);
        --tvhay-primary: #c98818;
        --tvhay-primary-container: #e5a93c;
        --tvhay-on-primary: #ffffff;
        --tvhay-secondary-container: #e10111;
        --tvhay-text: #111827;
        --tvhay-text-muted: #475569;
        --tvhay-outline: #64748b;
        --tvhay-border: rgba(15, 23, 42, 0.12);
        --tvhay-card-shadow: 0 6px 16px rgba(15, 23, 42, 0.1);
        /* Tương thích với biến trong cinema-noir.css và footer.jsp */
        --noir-bg: #ffffff;
        --noir-text: #111827;
        --noir-muted: #475569;
        --noir-border: #cbd5e1;
        --canvas: #f4f6f9;
        --surface-low: #ffffff;
        --surface-mid: #eaeff5;
        --surface-high: #dde3ea;
        --surface-stroke: #cbd5e1;
        --text-white: #111827;
        --text-primary: #111827;
    }

    body {
        background-color: var(--tvhay-bg) !important;
        color: var(--tvhay-text) !important;
        font-family: "Inter", sans-serif;
        transition: background-color 0.25s ease, color 0.25s ease;
    }

    .tvhay-header {
        position: sticky;
        top: 0;
        z-index: 1030;
        height: 64px;
        background: var(--tvhay-header-bg);
        backdrop-filter: blur(16px);
        box-shadow: 0 1px 8px rgba(0, 0, 0, 0.18);
        border-bottom: 1px solid var(--tvhay-border);
        transition: background-color 0.25s ease, border-color 0.25s ease;
    }

    .tvhay-header-inner {
        max-width: 1680px;
        height: 100%;
        margin: 0 auto;
        padding: 0 20px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
    }

    .tvhay-brand {
        display: flex;
        align-items: center;
        gap: 0;
        text-decoration: none;
        flex-shrink: 0;
        line-height: 0;
    }

    .site-header .tvhay-brand {
        margin: 0;
        padding: 0;
    }

    .site-header .tvhay-brand .site-logo {
        display: block;
        width: 124px;
        height: 34px;
        object-fit: contain;
        border-radius: 0;
        flex: 0 0 auto;
        transition: opacity 0.2s ease, transform 0.2s ease;
    }

    .site-header .tvhay-brand:hover .site-logo {
        opacity: 0.9;
        transform: translateY(-1px);
    }

    .tvhay-nav {
        display: flex;
        align-items: center;
        gap: 6px;
        height: 100%;
        margin: 0;
        padding: 0;
        list-style: none;
    }

    .tvhay-nav>li {
        height: 100%;
        display: flex;
        align-items: center;
        position: relative;
    }

    .tvhay-nav-link {
        height: 100%;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 0 10px;
        font-size: 13px;
        font-weight: 600;
        color: var(--tvhay-text-muted);
        text-decoration: none;
        transition: color 0.2s ease;
        white-space: nowrap;
    }

    .tvhay-nav-link:hover,
    .tvhay-nav-link.active {
        color: var(--tvhay-primary);
    }

    .tvhay-search-form {
        position: relative;
        flex: 1;
        max-width: 420px;
        margin: 0 12px;
    }

    .tvhay-search-icon {
        position: absolute;
        left: 14px;
        top: 50%;
        transform: translateY(-50%);
        color: var(--tvhay-outline);
        font-size: 18px;
        pointer-events: none;
    }

    .tvhay-search-input {
        width: 100%;
        height: 40px !important;
        padding: 0 16px 0 40px !important;
        border-radius: 9999px !important;
        background-color: var(--tvhay-surface) !important;
        border: 1px solid var(--tvhay-border) !important;
        color: var(--tvhay-text) !important;
        font-size: 13px !important;
        transition: all 0.2s ease;
    }

    .tvhay-search-input::placeholder {
        color: var(--tvhay-outline);
    }

    .tvhay-search-input:focus {
        outline: none !important;
        background-color: var(--tvhay-surface-low) !important;
        border-color: var(--tvhay-primary) !important;
        box-shadow: 0 0 12px rgba(229, 169, 60, 0.25) !important;
    }

    .tvhay-actions {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-shrink: 0;
    }

    .tvhay-btn-history {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 7px 12px;
        border-radius: 10px;
        background: var(--tvhay-surface);
        color: var(--tvhay-text-muted);
        border: 1px solid var(--tvhay-border);
        font-size: 13px;
        font-weight: 600;
        text-decoration: none;
        transition: all 0.2s ease;
    }

    .tvhay-btn-history:hover {
        background: var(--tvhay-surface-high);
        color: var(--tvhay-text);
    }

    .tvhay-btn-gold {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 8px 16px;
        border-radius: 10px;
        background: #ffc665;
        color: #432c00 !important;
        font-size: 13px;
        font-weight: 700;
        text-decoration: none;
        box-shadow: 0 0 12px rgba(229, 169, 60, 0.25);
        transition: all 0.2s ease;
        border: none;
    }

    .tvhay-btn-gold:hover {
        background: #e5a93c;
        transform: translateY(-1px);
    }

    .tvhay-avatar-pill {
        width: 34px;
        height: 34px;
        border-radius: 9999px;
        background: #ffc665;
        color: #432c00;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
    }

    .tvhay-theme-btn {
        width: 38px;
        height: 38px;
        border-radius: 10px;
        background: var(--tvhay-surface);
        border: 1px solid var(--tvhay-border);
        color: var(--tvhay-primary);
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0;
        cursor: pointer;
        font-size: 18px;
        transition: all 0.2s ease;
    }

    .tvhay-theme-btn:hover {
        background: var(--tvhay-surface-high);
        transform: scale(1.05);
    }

    @media (max-width: 991.98px) {
        .tvhay-nav {
            display: none;
        }
    }

    @media (max-width: 575.98px) {
        .tvhay-header-inner {
            gap: 10px;
            padding: 0 12px;
        }

        .tvhay-header-inner>.d-flex {
            gap: 8px !important;
        }

        .site-header .tvhay-brand .site-logo {
            width: 108px;
            height: 30px;
        }

        .tvhay-search-form {
            min-width: 0;
            margin: 0;
        }

        .tvhay-actions {
            gap: 6px;
        }
    }

</style>

<header class="site-header tvhay-header">
    <nav class="tvhay-header-inner" aria-label="Main navigation">
        <div class="d-flex align-items-center gap-4">
            <%-- LOGO --%>
            <a class="navbar-brand brand-mark tvhay-brand" href="${ctx}/home">
                <img class="site-logo" src="${ctx}/assets/img/cinema-logo.svg" alt="TVHAY+" />
            </a>

            <%-- MENU ĐIỀU HƯỚNG --%>
            <ul class="tvhay-nav">
                <li>
                    <a class="tvhay-nav-link" href="${ctx}/home">Trang Chủ</a>
                </li>
                <li>
                    <a class="tvhay-nav-link" href="${ctx}/movies?type=SERIES">Phim Bộ</a>
                </li>
                <li>
                    <a class="tvhay-nav-link" href="${ctx}/movies?type=SINGLE">Phim Lẻ</a>
                </li>
                <li>
                    <a class="tvhay-nav-link" href="${ctx}/movies">
                        <span>Quốc Gia</span>
                        <span class="material-symbols-outlined" style="font-size:16px;">expand_more</span>
                    </a>
                </li>
                <li>
                    <a class="tvhay-nav-link" href="${ctx}/movies">
                        <span>Thể Loại</span>
                        <span class="material-symbols-outlined" style="font-size:16px;">expand_more</span>
                    </a>
                </li>
                <li>
                    <a class="tvhay-nav-link" style="color:var(--tvhay-primary);" href="${ctx}/subscription">Gói VIP</a>
                </li>
            </ul>
        </div>

        <%-- THANH TÌM KIẾM LIVE SEARCH --%>
        <form class="live-search tvhay-search-form" action="${ctx}/movies" method="get" role="search">
            <label class="visually-hidden" for="liveSearchInput">Tìm phim</label>
            <span class="material-symbols-outlined tvhay-search-icon">search</span>
            <input id="liveSearchInput" class="form-control search-input tvhay-search-input" type="search" name="q"
                value="${fn:escapeXml(param.q)}" placeholder="Tìm kiếm..." autocomplete="off"
                data-search-url="${ctx}/api/movies/quick-search" data-detail-url="${ctx}/movie"
                data-movies-url="${ctx}/movies" />
            <div id="liveSearchResults" class="search-results" role="listbox" aria-label="Gợi ý tìm kiếm" hidden></div>
        </form>

        <%-- CỤM NÚT HÀNH ĐỘNG PHẢI --%>
        <div class="tvhay-actions">
            <a class="tvhay-btn-history d-none d-sm-inline-flex" href="${ctx}/account#watch-history">
                <span class="material-symbols-outlined"
                    style="font-size:18px; color:var(--tvhay-primary);">history</span>
                <span>Lịch sử xem</span>
            </a>

            <%-- NÚT CHUYỂN ĐỔI SÁNG / TỐI --%>
            <button class="theme-toggle tvhay-theme-btn" type="button" data-theme-url="${ctx}/api/theme"
                aria-label="Chuyển giao diện sáng tối" title="Chuyển giao diện Sáng / Tối">
                <span class="theme-icon" aria-hidden="true">☾</span>
            </button>

            <c:choose>
                <c:when test="${not empty sessionScope.loggedInUser}">
                    <c:if test="${sessionScope.loggedInUser.roleId eq 1}">
                        <a class="tvhay-btn-history d-none d-lg-inline-flex" href="${ctx}/admin">Quản trị</a>
                    </c:if>
                    <c:if test="${sessionScope.loggedInUser.roleId eq 2 or sessionScope.loggedInUser.roleId eq 8}">
                        <a class="tvhay-btn-history d-none d-lg-inline-flex" href="${ctx}/staff/rentals">Hỗ trợ vé</a>
                    </c:if>
                    <a class="tvhay-btn-gold" href="${ctx}/account">Tài khoản của tôi</a>
                    <a class="tvhay-avatar-pill" href="${ctx}/account" title="Hồ sơ cá nhân">
                        <span class="material-symbols-outlined" style="font-size:18px;">person</span>
                    </a>
                </c:when>
                <c:otherwise>
                    <a class="tvhay-btn-gold" href="${ctx}/login">Đăng nhập</a>
                    <a class="tvhay-avatar-pill" href="${ctx}/login" title="Đăng nhập">
                        <span class="material-symbols-outlined" style="font-size:18px;">person</span>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>
</header>

<script src="${ctx}/assets/js/live-search.js" defer></script>
<script src="${ctx}/assets/js/theme.js" defer></script>
