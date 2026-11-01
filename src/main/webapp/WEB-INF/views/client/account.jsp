<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme}">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Tài khoản của tôi | TVHAY+</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/account.css" />
    <meta name="csrf-token" content="${sessionScope.csrfToken}" />
</head>

<body class="account-page account-page--${roleCategory}">
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <header class="account-topbar">
        <a class="account-brand" href="${ctx}/home" aria-label="TVHAY+ trang chủ">
            <img src="${ctx}/assets/img/cinema-logo.svg" alt="TVHAY+" />
        </a>
        <nav class="topbar-links" aria-label="Điều hướng chính">
            <a href="${ctx}/home">Trang chủ</a>
            <a href="${ctx}/movies">Kho phim</a>
            <c:choose>
                <c:when test="${roleCategory eq 'admin'}"><a class="topbar-highlight" href="${ctx}/admin">Quản trị</a>
                </c:when>
                <c:when test="${roleCategory eq 'staff'}"><a class="topbar-highlight" href="${ctx}/staff/rentals">Hỗ trợ
                        vé</a></c:when>
                <c:otherwise><a class="topbar-highlight" href="${ctx}/subscription">Gói thành viên</a></c:otherwise>
            </c:choose>
        </nav>
        <div class="topbar-user">
            <span class="user-avatar" aria-hidden="true">
                <c:out
                    value="${empty profile.fullName ? profile.username.substring(0, 1) : profile.fullName.substring(0, 1)}" />
            </span>
            <span class="topbar-username">
                <c:out value="${profile.username}" />
            </span>
            <form action="${ctx}/logout" method="post">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <button class="button button-quiet" type="submit">Đăng xuất</button>
            </form>
        </div>
    </header>

    <div class="account-layout">
        <aside class="account-sidebar">
            <div class="sidebar-identity">
                <span class="role-symbol material-icon" aria-hidden="true">
                    <c:choose>
                        <c:when test="${roleCategory eq 'admin'}">shield_person</c:when>
                        <c:when test="${roleCategory eq 'staff'}">support_agent</c:when>
                        <c:when test="${roleCategory eq 'guest'}">person_outline</c:when>
                        <c:otherwise>movie</c:otherwise>
                    </c:choose>
                </span>
                <span class="eyebrow">KHÔNG GIAN CỦA BẠN</span>
                <strong>
                    <c:out value="${profile.fullName}" />
                </strong>
                <span class="role-badge role-badge--${roleCategory}">
                    <c:choose>
                        <c:when test="${roleCategory eq 'admin'}">QUẢN TRỊ VIÊN</c:when>
                        <c:when test="${roleCategory eq 'staff'}">NHÂN VIÊN</c:when>
                        <c:when test="${roleCategory eq 'guest'}">KHÁCH VÃNG LAI</c:when>
                        <c:otherwise>THÀNH VIÊN</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <nav class="side-nav" aria-label="Điều hướng tài khoản">
                <a class="side-link is-active" href="#overview"><span class="material-icon">dashboard</span>Tổng
                    quan</a>
                <c:choose>
                    <c:when test="${roleCategory eq 'admin'}">
                        <a class="side-link" href="${ctx}/admin"><span
                                class="material-icon">admin_panel_settings</span>Bảng
                            điều khiển</a>
                        <a class="side-link" href="${ctx}/admin/movies"><span class="material-icon">movie</span>Quản lý
                            phim</a>
                        <a class="side-link" href="${ctx}/admin/members"><span class="material-icon">group</span>Quản lý
                            thành viên</a>
                        <a class="side-link" href="${ctx}/admin/episodes"><span
                                class="material-icon">video_library</span>Tập phim &amp;
                            server</a>
                        <a class="side-link" href="${ctx}/admin/banners"><span class="material-icon">campaign</span>Quản
                            lý banner</a>
                        <a class="side-link" href="${ctx}/admin/comments"><span class="material-icon">forum</span>Kiểm
                            duyệt bình luận</a>
                    </c:when>
                    <c:when test="${roleCategory eq 'staff'}">
                        <a class="side-link" href="${ctx}/staff/rentals"><span
                                class="material-icon">confirmation_number</span>Hỗ trợ vé
                            thuê</a>
                        <a class="side-link" href="${ctx}/movies"><span class="material-icon">movie</span>Tra cứu
                            phim</a>
                    </c:when>
                    <c:otherwise>
                        <a class="side-link" href="#profile"><span class="material-icon">manage_accounts</span>Hồ sơ cá
                            nhân</a>
                        <a class="side-link" href="#watch-history"><span class="material-icon">history</span>Lịch sử
                            xem</a>
                        <c:if test="${roleCategory eq 'customer'}">
                            <a class="side-link" href="#rentals"><span
                                    class="material-icon">confirmation_number</span>Vé thuê
                                phim</a>
                        </c:if>
                        <a class="side-link" href="${ctx}/subscription"><span
                                class="material-icon">workspace_premium</span>Gói thành
                            viên</a>
                    </c:otherwise>
                </c:choose>
                <a class="side-link" href="#security"><span class="material-icon">security</span>Bảo mật</a>
            </nav>
            <div class="sidebar-footer">
                <span class="material-icon">help</span>
                <span>Cần hỗ trợ?</span>
                <a href="${ctx}/faq">Trung tâm trợ giúp</a>
            </div>
        </aside>

        <main class="account-main" id="overview">
            <c:if test="${param.message eq 'revoked'}">
                <div class="notice notice--success" role="status">
                    Đã thu hồi phiên thiết bị.
                </div>
            </c:if>
            <c:if test="${param.message eq 'password_updated'}">
                <div class="notice notice--success" role="status">
                    Đã đổi mật khẩu.
                </div>
            </c:if>
            <c:if test="${param.message eq 'password_invalid'}">
                <div class="notice notice--error" role="alert">
                    Mật khẩu cũ không chính xác hoặc mật khẩu mới không hợp lệ.
                </div>
            </c:if>

            <section class="welcome-panel">
                <div class="welcome-copy">
                    <span class="eyebrow">
                        <c:choose>
                            <c:when test="${roleCategory eq 'admin'}">ADMIN CONSOLE</c:when>
                            <c:when test="${roleCategory eq 'staff'}">STAFF WORKSPACE</c:when>
                            <c:when test="${roleCategory eq 'guest'}">GUEST EXPERIENCE</c:when>
                            <c:otherwise>MEMBER PROFILE</c:otherwise>
                        </c:choose>
                    </span>
                    <h1>
                        <c:choose>
                            <c:when test="${roleCategory eq 'admin'}">Xin chào, Quản trị viên</c:when>
                            <c:when test="${roleCategory eq 'staff'}">Xin chào,
                                <c:out value="${profile.fullName}" />
                            </c:when>
                            <c:when test="${roleCategory eq 'guest'}">Chào mừng bạn trải nghiệm</c:when>
                            <c:otherwise>Không gian phim của
                                <c:out value="${profile.fullName}" />
                            </c:otherwise>
                        </c:choose>
                    </h1>
                    <p>
                        <c:choose>
                            <c:when test="${roleCategory eq 'admin'}">Theo dõi hoạt động nền tảng và truy cập nhanh các
                                công cụ
                                quản trị.</c:when>
                            <c:when test="${roleCategory eq 'staff'}">Truy cập nhanh các công cụ hỗ trợ và quản lý vé
                                thuê
                                phim.</c:when>
                            <c:when test="${roleCategory eq 'guest'}">Tài khoản trải nghiệm giúp bạn khám phá phim và
                                lưu lịch sử
                                xem trên thiết bị này.</c:when>
                            <c:otherwise>Quản lý thông tin hội viên, xem tiếp nội dung yêu thích và
                                theo dõi quyền lợi của bạn.</c:otherwise>
                        </c:choose>
                    </p>
                    <span class="member-code">MÃ TÀI KHOẢN
                        <strong>
                            <c:out value="${profile.userCode}" />
                        </strong></span>
                </div>
                <div class="welcome-emblem" aria-hidden="true">
                    <span class="material-icon">
                        <c:choose>
                            <c:when test="${roleCategory eq 'admin'}">shield_person</c:when>
                            <c:when test="${roleCategory eq 'staff'}">support_agent</c:when>
                            <c:when test="${roleCategory eq 'guest'}">local_activity</c:when>
                            <c:otherwise>workspace_premium</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </section>

            <c:choose>
                <c:when test="${roleCategory eq 'admin'}">
                    <section class="section-block">
                        <div class="section-heading">
                            <div>
                                <span class="eyebrow">QUẢN TRỊ HỆ THỐNG</span>
                                <h2>Công cụ quản lý</h2>
                            </div>
                        </div>
                        <div class="portal-grid">
                            <a class="portal-card portal-card--accent" href="${ctx}/admin">
                                <span class="portal-icon material-icon">monitoring</span><strong>Bảng điều
                                    khiển</strong>
                                <span>Doanh thu, giao dịch và hoạt động nền tảng</span><b>Mở dashboard <span
                                        aria-hidden="true">→</span></b>
                            </a>
                            <a class="portal-card" href="${ctx}/admin/movies">
                                <span class="portal-icon material-icon">movie_edit</span><strong>Nội dung phim</strong>
                                <span>Thêm, cập nhật và quản lý trạng thái phim</span><b>Quản lý phim <span
                                        aria-hidden="true">→</span></b>
                            </a>
                            <a class="portal-card" href="${ctx}/admin/members">
                                <span class="portal-icon material-icon">groups</span><strong>Thành viên</strong>
                                <span>Tra cứu và quản lý tài khoản khách hàng</span><b>Quản lý thành viên <span
                                        aria-hidden="true">→</span></b>
                            </a>
                            <a class="portal-card" href="${ctx}/admin/banners">
                                <span class="portal-icon material-icon">campaign</span><strong>Giao diện &amp; cộng
                                    đồng</strong>
                                <span>Banner trang chủ và kiểm duyệt bình luận</span><b>Mở công cụ <span
                                        aria-hidden="true">→</span></b>
                            </a>
                        </div>
                    </section>
                </c:when>
                <c:when test="${roleCategory eq 'staff'}">
                    <section class="section-block">
                        <div class="section-heading">
                            <div>
                                <span class="eyebrow">TRUNG TÂM HỖ TRỢ</span>
                                <h2>Công việc của nhân viên</h2>
                            </div>
                        </div>
                        <div class="portal-grid portal-grid--staff">
                            <a class="portal-card portal-card--accent" href="${ctx}/staff/rentals">
                                <span class="portal-icon material-icon">confirmation_number</span><strong>Hỗ trợ vé
                                    thuê</strong>
                                <span>Tra cứu mã khách hàng, kiểm tra vé và lịch sử xử lý</span><b>Mở khu vực hỗ trợ
                                    <span aria-hidden="true">→</span></b>
                            </a>
                            <a class="portal-card" href="${ctx}/movies">
                                <span class="portal-icon material-icon">manage_search</span><strong>Tra cứu nội
                                    dung</strong>
                                <span>Tìm phim và kiểm tra thông tin hiển thị</span><b>Mở kho phim <span
                                        aria-hidden="true">→</span></b>
                            </a>
                        </div>
                    </section>
                </c:when>
                <c:when test="${roleCategory eq 'guest'}">
                    <section class="guest-banner">
                        <div>
                            <span class="eyebrow">BẮT ĐẦU HÀNH TRÌNH CỦA BẠN</span>
                            <h2>Tạo hồ sơ cá nhân để giữ lại trải nghiệm</h2>
                            <p>
                                Đăng ký tài khoản thành viên để quản lý hồ sơ và tiếp tục xem
                                trên nhiều phiên đăng nhập.
                            </p>
                        </div>
                        <a class="button button-primary" href="${ctx}/register">Tạo tài khoản thành viên</a>
                    </section>
                    <div class="summary-grid summary-grid--guest">
                        <article class="summary-card">
                            <span class="material-icon">play_circle</span><strong>Khám phá phim</strong>
                            <p>Tìm phim bộ, phim lẻ và nội dung mới.</p>
                            <a href="${ctx}/movies">Duyệt kho phim →</a>
                        </article>
                        <article class="summary-card">
                            <span class="material-icon">workspace_premium</span><strong>Trải nghiệm quyền lợi</strong>
                            <p>Tìm hiểu các gói xem và tùy chọn thuê phim.</p>
                            <a href="${ctx}/subscription">Xem gói thành viên →</a>
                        </article>
                        <article class="summary-card">
                            <span class="material-icon">history</span><strong>Lịch sử xem</strong>
                            <p>
                                <c:out value="${empty watchHistory ? 0 : watchHistory.size()}" />
                                nội dung đã ghi nhận cho tài khoản này.
                            </p>
                            <a href="#watch-history">Xem lịch sử →</a>
                        </article>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="summary-grid">
                        <article class="summary-card">
                            <span class="material-icon">workspace_premium</span><span class="summary-label">HẠNG HỘI
                                VIÊN</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty vipExpireDate}">VIP</c:when>
                                    <c:otherwise>Thành viên</c:otherwise>
                                </c:choose>
                            </strong>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty vipExpireDate}">Quyền lợi đến
                                        <c:out value="${vipExpireDate}" />
                                    </c:when>
                                    <c:otherwise>Khám phá quyền lợi xem phim cao cấp.</c:otherwise>
                                </c:choose>
                            </p>
                            <a href="${ctx}/subscription">Quản lý gói →</a>
                        </article>
                        <article class="summary-card">
                            <span class="material-icon">confirmation_number</span><span class="summary-label">VÉ THUÊ
                                CÒN HẠN</span>
                            <strong>
                                <c:out value="${empty activeRentals ? 0 : activeRentals.size()}" />
                            </strong>
                            <p>Phim bạn có thể xem ngay.</p>
                            <a href="#rentals">Xem vé thuê →</a>
                        </article>
                        <article class="summary-card">
                            <span class="material-icon">history</span><span class="summary-label">NỘI DUNG ĐÃ XEM</span>
                            <strong>
                                <c:out value="${empty watchHistory ? 0 : watchHistory.size()}" />
                            </strong>
                            <p>Lịch sử xem được đồng bộ với tài khoản.</p>
                            <a href="#watch-history">Xem lịch sử →</a>
                        </article>
                    </div>
                </c:otherwise>
            </c:choose>

            <section class="section-block profile-grid">
                <article class="content-card" id="profile">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">THÔNG TIN CÁ NHÂN</span>
                            <h2>Hồ sơ tài khoản</h2>
                        </div>
                    </div>
                    <dl class="profile-details">
                        <div>
                            <dt>Họ và tên</dt>
                            <dd>
                                <c:out value="${profile.fullName}" />
                            </dd>
                        </div>
                        <div>
                            <dt>Tên đăng nhập</dt>
                            <dd>
                                <c:out value="${profile.username}" />
                            </dd>
                        </div>
                        <div>
                            <dt>Email</dt>
                            <dd>
                                <c:out value="${profile.email}" />
                            </dd>
                        </div>
                        <div>
                            <dt>Vai trò</dt>
                            <dd>
                                <c:out value="${profile.role}" />
                            </dd>
                        </div>
                        <div>
                            <dt>Mã tài khoản</dt>
                            <dd>
                                <c:out value="${profile.userCode}" />
                            </dd>
                        </div>
                        <c:if test="${roleCategory eq 'customer'}">
                            <div>
                                <dt>Hạn VIP</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${not empty vipExpireDate}">
                                            <c:out value="${vipExpireDate}" />
                                        </c:when>
                                        <c:otherwise>Chưa đăng ký</c:otherwise>
                                    </c:choose>
                                </dd>
                            </div>
                        </c:if>
                    </dl>
                </article>
                <article class="content-card" id="security">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">AN TOÀN TÀI KHOẢN</span>
                            <h2>Đổi mật khẩu</h2>
                        </div>
                    </div>
                    <form action="${ctx}/account" method="post" class="password-form">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <input type="hidden" name="action" value="password" />
                        <label>Mật khẩu hiện tại<input type="password" name="oldPassword" required
                                autocomplete="current-password" /></label>
                        <label>Mật khẩu mới<input type="password" name="newPassword" minlength="10" maxlength="128"
                                required autocomplete="new-password" /></label>
                        <button class="button button-primary" type="submit">
                            Cập nhật mật khẩu
                        </button>
                    </form>
                </article>
            </section>

            <c:if test="${roleCategory eq 'customer' or roleCategory eq 'guest'}">
                <section class="section-block content-card" id="rentals">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">QUYỀN XEM CỦA BẠN</span>
                            <h2>Phim thuê còn hạn</h2>
                        </div>
                        <a href="${ctx}/subscription">Thuê phim <span aria-hidden="true">→</span></a>
                    </div>
                    <c:choose>
                        <c:when test="${empty activeRentals}">
                            <p class="empty-state">
                                Bạn chưa có phim thuê còn hạn.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <ul class="data-list">
                                <c:forEach items="${activeRentals}" var="r">
                                    <li>
                                        <span class="list-icon material-icon">movie</span><span
                                            class="list-main"><strong>
                                                <c:out value="${r.title}" />
                                            </strong><small>Quyền xem đến
                                                <c:out value="${r.expireAt}" />
                                            </small></span><span class="status-chip">Đang hoạt động</span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>
                <section class="section-block content-card" id="watch-history">
                    <div class="section-heading">
                        <div>
                            <span class="eyebrow">TIẾP TỤC GIẢI TRÍ</span>
                            <h2>Lịch sử xem gần đây</h2>
                        </div>
                        <a href="${ctx}/movies">Khám phá phim <span aria-hidden="true">→</span></a>
                    </div>
                    <c:choose>
                        <c:when test="${empty watchHistory}">
                            <p class="empty-state">
                                Chưa có lịch sử xem.
                                <a href="${ctx}/movies">Chọn một bộ phim để bắt đầu</a>.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <ul class="data-list watch-list">
                                <c:forEach items="${watchHistory}" var="entry">
                                    <li>
                                        <span class="list-icon material-icon">play_circle</span>
                                        <span class="list-main"><a
                                                href="${ctx}/watch?movieId=${entry.movieId}&amp;episodeId=${entry.episodeId}"><strong>
                                                    <c:out value="${entry.movieTitle}" />
                                                </strong></a><small>Tập
                                                <c:out value="${entry.episodeNumber}" />
                                            </small></span>
                                        <span class="watch-progress">
                                            <c:choose>
                                                <c:when test="${entry.completed}">Đã xem xong</c:when>
                                                <c:otherwise>
                                                    <c:out value="${entry.completionPercent}" />% đã
                                                    xem
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <a class="continue-link"
                                            href="${ctx}/watch?movieId=${entry.movieId}&amp;episodeId=${entry.episodeId}">Xem
                                            tiếp</a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>

            <section class="section-block content-card">
                <div class="section-heading">
                    <div>
                        <span class="eyebrow">QUẢN LÝ PHIÊN</span>
                        <h2>Thiết bị đã đăng nhập</h2>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty myDevices}">
                        <p class="empty-state">
                            Chưa có thiết bị được ghi nhận.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <ul class="data-list device-list">
                            <c:forEach items="${myDevices}" var="d">
                                <li>
                                    <span class="list-icon material-icon">devices</span>
                                    <span class="list-main"><strong>
                                            <c:out value="${d.name}" />
                                        </strong><small>Hoạt động gần nhất:
                                            <c:out value="${d.lastSeenAt}" />
                                        </small></span>
                                    <c:if test="${d.id eq currentDeviceId}"><span class="status-chip">Thiết bị hiện
                                            tại</span></c:if>
                                    <c:if test="${d.active and d.id ne currentDeviceId}">
                                        <form action="${ctx}/account" method="post">
                                            <input type="hidden" name="csrfToken"
                                                value="${sessionScope.csrfToken}" /><input type="hidden" name="action"
                                                value="revoke" /><input type="hidden" name="deviceId" value="${d.id}" />
                                            <button class="button button-danger-quiet" type="submit">
                                                Thu hồi
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${not d.active}"><span class="muted-status">Đã đăng xuất hoặc hết
                                            hạn</span></c:if>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="theme-row">
                <span><strong>Giao diện</strong><small>Tùy chỉnh chế độ hiển thị cho thiết bị này.</small></span><button
                    class="button button-quiet" type="button" data-theme-toggle>
                    Đổi sáng / tối
                </button>
            </section>
            <footer class="account-footer">
                <span>TVHAY+ · Không gian tài khoản riêng tư</span><a href="${ctx}/home">Quay lại trang chủ</a>
            </footer>
        </main>
    </div>
    <script src="${ctx}/assets/js/theme.js" defer></script>
</body>

</html>
