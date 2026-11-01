<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>TVHAY+ | Phim hay mỗi ngày</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
    <style>
        .tvhay-container {
            max-width: 1680px;
            margin: 0 auto;
            padding-left: 20px;
            padding-right: 20px;
        }

        /* HERO BANNER (Giữ nền điện ảnh tối để nổi chữ trắng trên ảnh bìa phim) */
        .tvhay-hero-section {
            position: relative;
            width: 100%;
            background-color: #0c0e14;
            overflow: hidden;
        }

        .tvhay-hero-slide {
            position: relative;
            width: 100%;
            min-height: clamp(360px, 42vw, 500px);
            background-image: var(--hero-image);
            background-size: cover;
            background-position: center;
            display: flex;
            align-items: center;
        }

        .tvhay-hero-grad-x {
            position: absolute;
            inset: 0;
            background: linear-gradient(90deg,
                    rgba(12, 14, 20, 0.95) 0%,
                    rgba(12, 14, 20, 0.8) 50%,
                    rgba(12, 14, 20, 0.15) 100%);
        }

        .tvhay-hero-grad-y {
            position: absolute;
            inset: 0;
            background: linear-gradient(0deg,
                    rgba(12, 14, 20, 0.95) 0%,
                    rgba(12, 14, 20, 0.35) 50%,
                    rgba(12, 14, 20, 0.7) 100%);
        }

        .tvhay-hero-content {
            position: relative;
            z-index: 10;
            max-width: 680px;
            padding: 80px 0 64px;
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .tvhay-hero-badges {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 8px;
        }

        .badge-hot-red {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 4px;
            background: #e10111;
            color: #fff1ef;
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .badge-dot-pulse {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: #ffffff;
        }

        .badge-series-dark {
            padding: 4px 10px;
            border-radius: 4px;
            background: rgba(40, 42, 48, 0.85);
            color: #ffc665;
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .badge-4k-gold {
            padding: 3px 8px;
            border-radius: 4px;
            background: #e5a93c;
            color: #432c00;
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
        }

        .tvhay-hero-title {
            font-family: "Be Vietnam Pro", sans-serif;
            font-size: clamp(28px, 4vw, 48px);
            font-weight: 800;
            line-height: 1.15;
            letter-spacing: -0.02em;
            text-transform: uppercase;
            color: #ffffff;
            text-shadow: 0 4px 16px rgba(0, 0, 0, 0.85);
            margin: 0;
        }

        .tvhay-hero-meta {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 12px;
            font-size: 13px;
            color: #d4c4b0;
        }

        .tvhay-hero-desc {
            font-size: 14px;
            line-height: 1.65;
            color: #d4c4b0;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            margin: 0;
            max-width: 580px;
        }

        .tvhay-hero-ctas {
            display: flex;
            align-items: center;
            gap: 14px;
            padding-top: 6px;
        }

        .btn-hero-play {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 24px;
            border-radius: 12px;
            background: #ffc665;
            color: #432c00 !important;
            font-family: "Be Vietnam Pro", sans-serif;
            font-size: 16px;
            font-weight: 700;
            text-decoration: none;
            box-shadow: 0 0 24px rgba(229, 169, 60, 0.4);
            transition: all 0.25s ease;
        }

        .btn-hero-play:hover {
            background: #e5a93c;
            transform: scale(1.02);
        }

        .btn-hero-secondary {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 12px 18px;
            border-radius: 12px;
            background: rgba(40, 42, 48, 0.82);
            color: #ffffff !important;
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            border: 1px solid rgba(255, 255, 255, 0.12);
            transition: all 0.2s ease;
        }

        .btn-hero-secondary:hover {
            background: #373940;
            color: #ffc665 !important;
        }

        .btn-hero-icon {
            width: 46px;
            height: 46px;
            border-radius: 12px;
            background: rgba(40, 42, 48, 0.82);
            color: #ffffff !important;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            border: 1px solid rgba(255, 255, 255, 0.12);
        }

        /* SECTION HEADER & FILTER TABS (Tự động đổi màu theo Sáng/Tối) */
        .tvhay-section-bar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            margin-bottom: 24px;
        }

        .tvhay-section-title-wrap {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .tvhay-gold-pill-bar {
            width: 6px;
            height: 26px;
            border-radius: 9999px;
            background: var(--tvhay-primary);
            box-shadow: 0 0 10px rgba(229, 169, 60, 0.45);
        }

        .tvhay-section-title {
            font-family: "Be Vietnam Pro", sans-serif;
            font-size: 26px;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--tvhay-text);
            margin: 0;
            letter-spacing: -0.015em;
        }

        .tvhay-filter-tabs {
            display: inline-flex;
            padding: 4px;
            border-radius: 12px;
            background: var(--tvhay-surface);
            border: 1px solid var(--tvhay-border);
            gap: 4px;
        }

        .tvhay-tab-btn {
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 600;
            color: var(--tvhay-text-muted);
            text-decoration: none;
            border: none;
            background: transparent;
            transition: all 0.2s ease;
        }

        .tvhay-tab-btn.active,
        .tvhay-tab-btn:hover {
            background: var(--tvhay-surface-low);
            color: var(--tvhay-primary);
            font-weight: 700;
        }

        /* MOVIE CARD 2:3 (Tự động đổi màu chữ dưới poster theo Sáng/Tối) */
        .tvhay-card {
            display: flex;
            flex-direction: column;
            width: 100%;
        }

        .tvhay-card-link {
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
        }

        .tvhay-card-poster {
            position: relative;
            width: 100%;
            aspect-ratio: 2 / 3;
            border-radius: 8px;
            overflow: hidden;
            background: var(--tvhay-surface-low);
            box-shadow: var(--tvhay-card-shadow);
            border: 1px solid var(--tvhay-border);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .tvhay-card-link:hover .tvhay-card-poster {
            transform: translateY(-6px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.35);
        }

        .tvhay-card-poster img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s ease;
        }

        .tvhay-card-link:hover .tvhay-card-poster img {
            transform: scale(1.05);
        }

        .tvhay-poster-scrim {
            position: absolute;
            inset: 0;
            background: linear-gradient(0deg,
                    rgba(17, 19, 25, 0.75) 0%,
                    transparent 45%);
            pointer-events: none;
        }

        .tvhay-badge-top-right {
            position: absolute;
            top: 8px;
            right: 8px;
            padding: 2px 8px;
            border-radius: 4px;
            background: #e5a93c;
            color: #432c00;
            font-size: 10px;
            font-weight: 800;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            z-index: 2;
        }

        .tvhay-hover-overlay {
            position: absolute;
            inset: 0;
            background: rgba(17, 19, 25, 0.45);
            backdrop-filter: blur(2px);
            display: flex;
            align-items: center;
            justify-content: center;
            opacity: 0;
            transition: opacity 0.3s ease;
            z-index: 3;
        }

        .tvhay-card-link:hover .tvhay-hover-overlay {
            opacity: 1;
        }

        .tvhay-play-circle {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            background: #ffc665;
            color: #432c00;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 0 16px rgba(229, 169, 60, 0.65);
        }

        .tvhay-card-caption {
            padding-top: 10px;
            display: flex;
            flex-direction: column;
            gap: 2px;
            min-width: 0;
        }

        .tvhay-card-title {
            font-size: 15px;
            font-weight: 600;
            color: var(--tvhay-text);
            margin: 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            transition: color 0.2s ease;
        }

        .tvhay-card-link:hover .tvhay-card-title {
            color: var(--tvhay-primary);
        }

        .tvhay-card-sub {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 12px;
        }

        .tvhay-card-audio {
            color: var(--tvhay-primary);
            font-weight: 600;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .tvhay-card-year {
            color: var(--tvhay-outline);
            flex-shrink: 0;
        }

        .tvhay-card-views {
            display: flex;
            align-items: center;
            gap: 4px;
            color: var(--tvhay-outline);
            font-size: 11px;
            font-weight: 600;
        }

        .btn-load-more {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            min-width: 280px;
            padding: 13px 28px;
            border-radius: 12px;
            background: var(--tvhay-surface);
            border: 1px solid var(--tvhay-border);
            color: var(--tvhay-text) !important;
            font-size: 13px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.25s ease;
        }

        .btn-load-more:hover {
            background: var(--tvhay-surface-high);
            color: var(--tvhay-primary) !important;
        }

        .tvhay-continue-box {
            background: var(--tvhay-surface-low);
            border: 1px solid var(--tvhay-border);
            box-shadow: var(--tvhay-card-shadow);
        }

    </style>
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />

    <main>
        <%-- 1. HERO BANNER / CAROUSEL SECTION --%>
        <c:choose>
            <c:when test="${not empty banners}">
                <section id="heroCarousel" class="carousel slide tvhay-hero-section" data-bs-ride="carousel"
                    aria-label="Phim nổi bật">
                    <div class="carousel-indicators" style="margin-bottom: 22px;">
                        <c:forEach items="${banners}" var="banner" varStatus="loop">
                            <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="${loop.index}"
                                class="${loop.first ? 'active' : ''}" aria-label="Banner ${loop.count}"
                                style="width:${loop.first ? '36px' : '14px'}; height:6px; border-radius:999px; background-color:${loop.first ? '#ffc665' : '#33353b'}; border:none;"></button>
                        </c:forEach>
                    </div>
                    <div class="carousel-inner">
                        <c:forEach items="${banners}" var="banner" varStatus="loop">
                            <div class="carousel-item ${loop.first ? 'active' : ''}">
                                <c:set var="bannerBg"
                                    value="${banner.imagePath.startsWith('http') ? banner.imagePath : (banner.imagePath.startsWith('/') ? pageContext.request.contextPath.concat(banner.imagePath) : pageContext.request.contextPath.concat('/images/').concat(banner.imagePath))}" />
                                <div class="tvhay-hero-slide" style="--hero-image: url('${bannerBg}')">
                                    <div class="tvhay-hero-grad-x"></div>
                                    <div class="tvhay-hero-grad-y"></div>
                                    <div class="tvhay-container w-100">
                                        <div class="tvhay-hero-content">
                                            <div class="tvhay-hero-badges">
                                                <span class="badge-hot-red">
                                                    <span class="badge-dot-pulse"></span>
                                                    Hot Trending #${loop.count}
                                                </span>
                                                <span class="badge-series-dark">TVHAY+ ORIGINAL</span>
                                                <span class="badge-4k-gold">4K UHD</span>
                                            </div>

                                            <h1 class="tvhay-hero-title">
                                                <c:out
                                                    value="${not empty banner.movieTitle ? banner.movieTitle : banner.title}" />
                                            </h1>

                                            <div class="tvhay-hero-meta">
                                                <span style="color:#ffc665; font-weight:700;">★ 9.2
                                                    <small style="color:#9d8f7c;">/10 IMDb</small></span>
                                                <span>•</span>
                                                <span class="px-2 py-1 rounded"
                                                    style="background:#282a30; color:#e2e2ea; font-size:11px; font-weight:700;">2025</span>
                                                <span>•</span>
                                                <span>Thuyết minh &amp; Vietsub</span>
                                                <span>•</span>
                                                <span style="color:#ffc665; font-weight:700;">Full HD / 4K</span>
                                            </div>

                                            <p class="tvhay-hero-desc">
                                                <c:out value="${banner.description}" />
                                            </p>

                                            <div class="tvhay-hero-ctas">
                                                <a class="btn-hero-play"
                                                    href="${pageContext.request.contextPath}/movie?id=${banner.movieId}">
                                                    <span class="material-symbols-outlined"
                                                        style="font-variation-settings:'FILL' 1;">play_arrow</span>
                                                    <span>Xem ngay</span>
                                                </a>
                                                <a class="btn-hero-secondary"
                                                    href="${pageContext.request.contextPath}/movies">
                                                    <span class="material-symbols-outlined"
                                                        style="font-size:20px;">add</span>
                                                    <span>Danh sách</span>
                                                </a>
                                                <a class="btn-hero-icon"
                                                    href="${pageContext.request.contextPath}/movie?id=${banner.movieId}"
                                                    aria-label="Thông tin chi tiết">
                                                    <span class="material-symbols-outlined"
                                                        style="font-size:22px;">info</span>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel"
                        data-bs-slide="prev" style="width:5%;">
                        <span class="carousel-control-prev-icon"></span>
                        <span class="visually-hidden">Trước</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel"
                        data-bs-slide="next" style="width:5%;">
                        <span class="carousel-control-next-icon"></span>
                        <span class="visually-hidden">Sau</span>
                    </button>
                </section>
            </c:when>
            <c:otherwise>
                <section class="tvhay-hero-section">
                    <div class="tvhay-hero-slide"
                        style="--hero-image: url('${pageContext.request.contextPath}/assets/img/banner-sample.png')">
                        <div class="tvhay-hero-grad-x"></div>
                        <div class="tvhay-hero-grad-y"></div>
                        <div class="tvhay-container w-100">
                            <div class="tvhay-hero-content">
                                <div class="tvhay-hero-badges">
                                    <span class="badge-hot-red"><span class="badge-dot-pulse"></span>Hot Trending
                                        #1</span>
                                    <span class="badge-series-dark">Phim Bộ Series</span>
                                    <span class="badge-4k-gold">4K UHD</span>
                                </div>
                                <h1 class="tvhay-hero-title">
                                    CHUYỆN HAY BẮT ĐẦU
                                    <span style="color:#ffc665;">TỪ MỘT BỘ PHIM</span>
                                </h1>
                                <p class="tvhay-hero-desc">
                                    Khám phá phim mới, series hấp dẫn và nhiều nội dung đặc sắc
                                    chuẩn điện ảnh.
                                </p>
                                <div class="tvhay-hero-ctas">
                                    <a class="btn-hero-play" href="${pageContext.request.contextPath}/movies">
                                        <span class="material-symbols-outlined"
                                            style="font-variation-settings:'FILL' 1;">play_arrow</span>
                                        <span>Khám phá phim</span>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            </c:otherwise>
        </c:choose>

        <%-- 2. CATALOG PHIM CHÍNH --%>
        <div class="tvhay-container py-5">
            <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />

            <%-- CỤM 1: MỚI CẬP NHẬT / PHIM THỊNH HÀNH --%>
            <section class="mb-5">
                <div class="tvhay-section-bar">
                    <div class="tvhay-section-title-wrap">
                        <div class="tvhay-gold-pill-bar"></div>
                        <h2 class="tvhay-section-title">Mới Cập Nhật</h2>
                    </div>
                    <div class="d-flex align-items-center gap-3 flex-wrap">
                        <div class="tvhay-filter-tabs" role="tablist">
                            <a class="tvhay-tab-btn active" href="${pageContext.request.contextPath}/movies">Tất cả</a>
                            <a class="tvhay-tab-btn" href="${pageContext.request.contextPath}/movies?type=SERIES">Phim
                                bộ mới</a>
                            <a class="tvhay-tab-btn" href="${pageContext.request.contextPath}/movies?type=SINGLE">Phim
                                lẻ mới</a>
                            <a class="tvhay-tab-btn" href="${pageContext.request.contextPath}/movies?genreId=3">Hoạt
                                hình 3D</a>
                        </div>
                    </div>
                </div>

                <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-6 g-3 g-lg-4">
                    <c:forEach items="${trendingMovies}" var="movie">
                        <c:set var="cardMovie" value="${movie}" scope="request" />
                        <div class="col">
                            <jsp:include page="/WEB-INF/include/movie-card.jsp" />
                        </div>
                    </c:forEach>
                </div>
                <c:if test="${empty trendingMovies}">
                    <p class="empty-state">Danh sách phim sẽ sớm được cập nhật.</p>
                </c:if>

                <div class="w-100 pt-4 text-center">
                    <a class="btn-load-more" href="${pageContext.request.contextPath}/movies">
                        <span>Xem thêm phim mới cập nhật</span>
                        <span class="material-symbols-outlined" style="font-size:18px;">expand_more</span>
                    </a>
                </div>
            </section>

            <%-- CỤM 2: PHIM BỘ MỚI NHẤT --%>
            <c:if test="${not empty latestSeries}">
                <section class="mb-5">
                    <div class="tvhay-section-bar">
                        <div class="tvhay-section-title-wrap">
                            <div class="tvhay-gold-pill-bar"></div>
                            <h2 class="tvhay-section-title">Phim Bộ Mới Nhất</h2>
                        </div>
                        <a class="tvhay-tab-btn active" href="${pageContext.request.contextPath}/movies?type=SERIES">
                            Xem tất cả <span aria-hidden="true">→</span>
                        </a>
                    </div>
                    <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-6 g-3 g-lg-4">
                        <c:forEach items="${latestSeries}" var="movie">
                            <c:set var="cardMovie" value="${movie}" scope="request" />
                            <div class="col">
                                <jsp:include page="/WEB-INF/include/movie-card.jsp" />
                            </div>
                        </c:forEach>
                    </div>
                </section>
            </c:if>

            <%-- CỤM 3: PHIM LẺ MỚI NHẤT --%>
            <c:if test="${not empty latestSingles}">
                <section class="mb-5">
                    <div class="tvhay-section-bar">
                        <div class="tvhay-section-title-wrap">
                            <div class="tvhay-gold-pill-bar"></div>
                            <h2 class="tvhay-section-title">Phim Lẻ Mới Nhất</h2>
                        </div>
                        <a class="tvhay-tab-btn active" href="${pageContext.request.contextPath}/movies?type=SINGLE">
                            Xem tất cả <span aria-hidden="true">→</span>
                        </a>
                    </div>
                    <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-6 g-3 g-lg-4">
                        <c:forEach items="${latestSingles}" var="movie">
                            <c:set var="cardMovie" value="${movie}" scope="request" />
                            <div class="col">
                                <jsp:include page="/WEB-INF/include/movie-card.jsp" />
                            </div>
                        </c:forEach>
                    </div>
                </section>
            </c:if>

            <%-- CỤM 4: TIẾP TỤC XEM (VẠCH ĐỎ TIẾN TRÌNH) --%>
            <c:if test="${not empty continueWatching}">
                <section class="mb-4">
                    <div class="tvhay-section-bar">
                        <div class="tvhay-section-title-wrap">
                            <div class="tvhay-gold-pill-bar"></div>
                            <h2 class="tvhay-section-title">
                                Tiếp Tục Hành Trình Đang Xem
                            </h2>
                        </div>
                    </div>
                    <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-5 g-3">
                        <c:forEach items="${continueWatching}" var="entry">
                            <div class="col">
                                <a class="continue-card tvhay-continue-box d-block p-3 rounded-3 text-decoration-none"
                                    href="${pageContext.request.contextPath}/watch?movieId=${entry.movieId}&amp;episodeId=${entry.episodeId}">
                                    <span class="continue-title d-block fw-bold text-truncate mb-2"
                                        style="color:var(--tvhay-text);">
                                        <c:out value="${entry.movieTitle}" />
                                    </span>
                                    <span class="progress d-block rounded-pill overflow-hidden mb-1"
                                        style="height:6px; background:var(--tvhay-surface-highest);">
                                        <span class="d-block h-100"
                                            style="width:${entry.completionPercent}%; background:#e10111;"></span>
                                    </span>
                                    <small
                                        style="color:var(--tvhay-primary); font-size:12px;">${entry.completionPercent}%
                                        đã xem</small>
                                </a>
                            </div>
                        </c:forEach>
                    </div>
                </section>
            </c:if>
        </div>
    </main>

    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script src="${ctx}/assets/js/theme.js" defer></script>
</body>

</html>
