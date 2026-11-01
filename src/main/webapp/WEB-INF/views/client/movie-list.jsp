<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html class="dark" lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>
        <c:out value="${not empty pageTitle ? pageTitle : 'Danh sách phim'}" /> | TVHAY+
    </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
    <style>
        .tvhay-list-container {
            max-width: 1680px;
            margin: 0 auto;
            padding: 36px 20px 60px;
        }

        .tvhay-filter-box {
            background: #191b22;
            border: 1px solid rgba(255, 255, 255, 0.06);
            border-radius: 14px;
            padding: 16px;
            margin-bottom: 28px;
        }

        .tvhay-filter-box .form-control,
        .tvhay-filter-box .form-select {
            background-color: #1d1f26;
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: #e2e2ea;
            height: 44px;
            border-radius: 10px;
            font-size: 14px;
        }

        .tvhay-filter-box .form-control:focus,
        .tvhay-filter-box .form-select:focus {
            border-color: #ffc665;
            box-shadow: 0 0 0 2px rgba(255, 198, 101, 0.2);
        }

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
            background: #191b22;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.45);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .tvhay-card-link:hover .tvhay-card-poster {
            transform: translateY(-6px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.85);
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
                    rgba(17, 19, 25, 0.88) 0%,
                    transparent 50%);
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
            text-transform: uppercase;
            z-index: 2;
        }

        .tvhay-hover-overlay {
            position: absolute;
            inset: 0;
            background: rgba(17, 19, 25, 0.5);
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
            color: #e2e2ea;
            margin: 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .tvhay-card-link:hover .tvhay-card-title {
            color: #ffc665;
        }

        .tvhay-card-sub {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 12px;
        }

        .tvhay-card-audio {
            color: #ffc665;
            font-weight: 600;
        }

        .tvhay-card-year {
            color: #9d8f7c;
        }

        .tvhay-card-views {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #9d8f7c;
            font-size: 11px;
            font-weight: 600;
        }

    </style>
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />

    <main class="tvhay-list-container">
        <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />

        <%-- Tiêu đề trang có thanh dọc vàng --%>
        <div class="d-flex align-items-center gap-2 mb-4">
            <div
                style="width:6px; height:28px; border-radius:999px; background:#ffc665; box-shadow:0 0 10px rgba(229,169,60,0.5);">
            </div>
            <div>
                <span class="d-block" style="font-size:11px; font-weight:700; letter-spacing:0.08em; color:#ffc665;">KHO
                    PHIM TVHAY+</span>
                <h1 class="m-0 text-uppercase"
                    style="font-family:'Be Vietnam Pro',sans-serif; font-size:28px; font-weight:800;">
                    <c:out value="${not empty pageTitle ? pageTitle : 'Khám Phá Kho Phim'}" />
                </h1>
            </div>
        </div>

        <%-- Thanh bộ lọc phim --%>
        <form class="tvhay-filter-box row g-2 align-items-center" action="${pageContext.request.contextPath}/movies"
            method="get">
            <input type="hidden" name="pageSize" value="20" />
            <div class="col-12 col-md-4">
                <label class="visually-hidden" for="filterQuery">Từ khóa</label>
                <input class="form-control" id="filterQuery" name="q"
                    value="${fn:escapeXml(not empty q ? q : param.q)}"
                    placeholder="Nhập tên phim, diễn viên, đạo diễn..." />
            </div>
            <div class="col-6 col-md-2">
                <label class="visually-hidden" for="genreFilter">Thể loại</label>
                <select class="form-select" id="genreFilter" name="genreId">
                    <option value="">Tất cả thể loại</option>
                    <c:forEach items="${genres}" var="genre">
                        <option value="${genre.genreId}" ${(param.genreId eq genre.genreId or selectedGenreId eq
                            genre.genreId) ? 'selected' : '' }>
                            ${genre.genreName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <label class="visually-hidden" for="typeFilter">Loại phim</label>
                <select class="form-select" id="typeFilter" name="type">
                    <option value="">Tất cả loại phim</option>
                    <option value="SERIES" ${type eq 'SERIES' ? 'selected' : ''}>Phim bộ</option>
                    <option value="SINGLE" ${type eq 'SINGLE' ? 'selected' : ''}>Phim lẻ</option>
                    <option value="ANIMATION_3D" ${type eq 'ANIMATION_3D' ? 'selected' : ''}>Hoạt hình 3D</option>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <label class="visually-hidden" for="yearFilter">Năm</label>
                <select class="form-select" id="yearFilter" name="year">
                    <option value="">Mọi năm</option>
                    <c:forEach items="${years}" var="y">
                        <option value="${y}" ${(param.year eq y or selectedYear eq y) ? 'selected' : '' }>
                            ${y}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <button class="tvhay-btn-gold w-100" style="height:44px;" type="submit">
                    Lọc phim
                </button>
            </div>
        </form>

        <%-- Lưới 6 cột đồng bộ home_2.html --%>
        <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-lg-6 g-3 g-lg-4">
            <c:forEach items="${movies}" var="movie">
                <c:set var="cardMovie" value="${movie}" scope="request" />
                <div class="col">
                    <jsp:include page="/WEB-INF/include/movie-card.jsp" />
                </div>
            </c:forEach>
        </div>

        <c:if test="${empty movies}">
            <div class="text-center py-5 my-4 rounded-3" style="background:#191b22;">
                <p class="mb-0" style="color:#d4c4b0;">
                    Không tìm thấy phim phù hợp. Hãy thử thay đổi bộ lọc tìm kiếm.
                </p>
            </div>
        </c:if>

        <jsp:include page="/WEB-INF/include/pagination.jsp" />
    </main>

    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
    <script src="${ctx}/assets/js/theme.js" defer></script>
</body>

</html>
