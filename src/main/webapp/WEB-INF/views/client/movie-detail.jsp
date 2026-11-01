<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% if (session.getAttribute("csrfToken") == null) {
session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString()); } %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>
        <c:out value="${movie.title}" /> | TVHAY+
    </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cinema-noir.css" />
    <style>
        .tvhay-detail-hero {
            position: relative;
            min-height: 480px;
            background-image: var(--hero-image);
            background-size: cover;
            background-position: center;
            display: flex;
            align-items: flex-end;
            padding: 80px 0 48px;
        }

        .tvhay-detail-hero::before {
            content: "";
            position: absolute;
            inset: 0;
            background: linear-gradient(90deg,
                    rgba(12, 14, 20, 0.95) 0%,
                    rgba(12, 14, 20, 0.82) 55%,
                    rgba(12, 14, 20, 0.3) 100%),
                linear-gradient(0deg, rgba(12, 14, 20, 0.95) 0%, transparent 65%);
        }

        .tvhay-detail-container {
            max-width: 1680px;
            margin: 0 auto;
            padding: 0 20px;
            position: relative;
            z-index: 5;
            width: 100%;
        }

        .tvhay-detail-card {
            background: var(--tvhay-surface-low);
            border: 1px solid var(--tvhay-border);
            box-shadow: var(--tvhay-card-shadow);
            border-radius: 14px;
            padding: 24px;
            color: var(--tvhay-text);
        }

        .tvhay-input-theme {
            background-color: var(--tvhay-surface) !important;
            color: var(--tvhay-text) !important;
            border-color: var(--tvhay-border) !important;
        }

        .tvhay-ep-chip {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 56px;
            height: 40px;
            padding: 0 14px;
            border-radius: 8px;
            background: var(--tvhay-surface);
            border: 1px solid var(--tvhay-border);
            color: var(--tvhay-text);
            font-weight: 700;
            font-size: 13px;
            text-decoration: none;
            transition: all 0.2s ease;
        }

        .tvhay-ep-chip:hover {
            background: #ffc665;
            color: #432c00;
            border-color: #ffc665;
        }

    </style>
</head>

<body>
    <jsp:include page="/WEB-INF/include/header.jsp" />

    <main>
        <c:set var="backdropSrc"
            value="${not empty movie.backdropPath and movie.backdropPath.startsWith('http') ? movie.backdropPath : pageContext.request.contextPath.concat('/images/').concat(movie.backdropPath)}" />
        <section class="tvhay-detail-hero" style="--hero-image:url('${backdropSrc}')">
            <div class="tvhay-detail-container">
                <div style="max-width: 760px;">
                    <div class="d-flex align-items-center gap-2 mb-2">
                        <span class="px-2 py-1 rounded"
                            style="background:#e5a93c; color:#432c00; font-size:11px; font-weight:800;">
                            ${movie.releaseYear}
                        </span>
                        <c:if test="${not empty movie.quality}">
                            <span class="px-2 py-1 rounded"
                                style="background:#282a30; color:#ffc665; font-size:11px; font-weight:800;">
                                <c:out value="${movie.quality}" />
                            </span>
                        </c:if>
                        <c:if test="${movie.vipOnly}">
                            <span class="px-2 py-1 rounded"
                                style="background:#e10111; color:#ffffff; font-size:11px; font-weight:800;">
                                VIP EXCLUSIVE
                            </span>
                        </c:if>
                        <c:if test="${movie.rentOnly}">
                            <span class="px-2 py-1 rounded"
                                style="background:#ffc665; color:#432c00; font-size:11px; font-weight:800;">
                                PHIM CHIẾU RẠP 48H
                            </span>
                        </c:if>
                    </div>

                    <h1 class="text-uppercase fw-bolder mb-2"
                        style="font-family:'Be Vietnam Pro',sans-serif; font-size:clamp(28px,4vw,44px); color:#ffffff;">
                        <c:out value="${movie.title}" />
                    </h1>

                    <p class="mb-3" style="color:#d4c4b0; font-size:14px;">
                        <c:out value="${movie.country}" />
                        <c:if test="${movie.durationMinutes gt 0}">
                            •
                            <c:out value="${movie.durationMinutes}" /> phút
                        </c:if>
                        <c:if test="${not empty movie.genres}">
                            •
                            <c:out value="${movie.genres}" />
                        </c:if>
                    </p>

                    <p class="mb-4" style="color:#d4c4b0; line-height:1.7;">
                        <c:out value="${movie.synopsis}" />
                    </p>

                    <div class="d-flex flex-wrap align-items-center gap-3">
                        <c:choose>
                            <c:when test="${movie.rentOnly and not canWatch}">
                                <a class="tvhay-btn-gold px-4 py-3 rounded-3 fs-6"
                                    href="${pageContext.request.contextPath}/vip?rentMovieId=${not empty movie.id ? movie.id : movie.movieId}">
                                    🎟 Thuê Vé 48H (
                                    <c:out value="${movie.rentalPrice}" />đ)
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a class="tvhay-btn-gold px-4 py-3 rounded-3 fs-6"
                                    href="${pageContext.request.contextPath}/watch?movieId=${not empty movie.id ? movie.id : movie.movieId}${not empty firstEpisode ? '&episodeId='.concat(firstEpisode.episodeId) : ''}">
                                    ▶ Xem phim ngay
                                </a>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${not empty movie.trailerUrl}">
                            <button type="button" class="btn btn-outline-light px-4 py-3 rounded-3 fw-semibold"
                                onclick="openTrailerModal()">
                                🎬 Xem trailer trực tiếp
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </section>

        <div class="tvhay-detail-container py-5">
            <jsp:include page="/WEB-INF/include/toast-feedback.jsp" />

            <div class="row g-4">
                <div class="col-12 col-lg-8">
                    <div class="tvhay-detail-card h-100">
                        <span
                            style="color:var(--tvhay-primary); font-size:11px; font-weight:700; letter-spacing:0.08em;">
                            GIỚI THIỆU
                        </span>
                        <h2 class="h4 fw-bold mt-1 mb-3">Nội dung phim</h2>
                        <p style="color:var(--tvhay-text-muted); line-height:1.75;">
                            <c:out value="${movie.synopsis}" />
                        </p>

                        <c:if test="${not empty movie.cast}">
                            <h3 class="h6 fw-bold mt-4" style="color:var(--tvhay-primary);">
                                Diễn viên
                            </h3>
                            <p class="mb-0" style="color:var(--tvhay-text-muted);">
                                <c:out value="${movie.cast}" />
                            </p>
                        </c:if>
                        <c:if test="${not empty movie.director}">
                            <h3 class="h6 fw-bold mt-3" style="color:var(--tvhay-primary);">
                                Đạo diễn
                            </h3>
                            <p class="mb-0" style="color:var(--tvhay-text-muted);">
                                <c:out value="${movie.director}" />
                            </p>
                        </c:if>
                    </div>
                </div>

                <div class="col-12 col-lg-4">
                    <aside class="tvhay-detail-card text-center h-100 d-flex flex-column justify-content-center">
                        <span
                            style="color:var(--tvhay-primary); font-size:11px; font-weight:700; letter-spacing:0.08em;">
                            ĐÁNH GIÁ IMDb / CỘNG ĐỒNG
                        </span>
                        <div class="display-4 fw-bolder my-2" style="color:var(--tvhay-primary);">
                            ★ ${empty averageRating ? '9.0' : averageRating}
                            <small class="fs-5" style="color:var(--tvhay-outline);">/ 10</small>
                        </div>
                        <p class="mb-0" style="color:var(--tvhay-text-muted);">
                            ${ratingCount} lượt đánh giá
                        </p>
                    </aside>
                </div>
            </div>

            <%-- DANH SÁCH TẬP XEM TRỰC TIẾP --%>
            <c:if test="${not empty episodes}">
                <section class="tvhay-detail-card mt-4">
                    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
                        <div class="d-flex align-items-center gap-2">
                            <div style="width:6px; height:22px; border-radius:999px; background:var(--tvhay-primary);">
                            </div>
                            <h2 class="h5 fw-bold m-0 text-uppercase">
                                Danh Sách Tập Phát Sóng (${fn:length(episodes)} tập)
                            </h2>
                        </div>
                        <span class="small" style="color:var(--tvhay-text-muted);">Chọn tập để phát trực tiếp</span>
                    </div>
                    <div class="d-flex flex-wrap gap-2">
                        <c:forEach items="${episodes}" var="epItem">
                            <a class="tvhay-ep-chip"
                                href="${pageContext.request.contextPath}/watch?movieId=${not empty movie.id ? movie.id : movie.movieId}&episodeId=${epItem.episodeId}"
                                title="${epItem.episodeTitle}">
                                Tập
                                <c:out
                                    value="${epItem.episodeNumber lt 10 ? '0'.concat(epItem.episodeNumber) : epItem.episodeNumber}" />
                            </a>
                        </c:forEach>
                    </div>
                </section>
            </c:if>

            <%-- BÌNH LUẬN CỘNG ĐỒNG --%>
            <section class="mt-5">
                <div class="d-flex align-items-center gap-2 mb-4">
                    <div style="width:6px; height:24px; border-radius:999px; background:var(--tvhay-primary);"></div>
                    <h2 class="h4 fw-bold m-0 text-uppercase" style="color:var(--tvhay-text);">
                        Bình Luận Cộng Đồng
                    </h2>
                </div>

                <c:choose>
                    <c:when test="${not empty sessionScope.loggedInUser or not empty sessionScope.user}">
                        <form action="${pageContext.request.contextPath}/comment" method="post"
                            class="tvhay-detail-card mb-4">
                            <input type="hidden" name="action" value="create" />
                            <input type="hidden" name="movieId"
                                value="${not empty movie.id ? movie.id : movie.movieId}" />
                            <input type="hidden" name="csrfToken"
                                value="${not empty sessionScope.csrfToken ? sessionScope.csrfToken : pageContext.session.id}" />
                            <label for="rating" class="form-label fw-semibold" style="color:var(--tvhay-primary);">
                                Đánh giá của bạn (1 - 10 sao)
                            </label>
                            <select class="form-select mb-3 tvhay-input-theme" id="rating" name="rating" required>
                                <option value="">Chọn điểm đánh giá</option>
                                <c:forEach begin="1" end="10" var="star">
                                    <option value="${star}">${star} ★</option>
                                </c:forEach>
                            </select>
                            <label class="visually-hidden" for="commentContent">Nội dung bình luận</label>
                            <textarea id="commentContent" class="form-control mb-3 tvhay-input-theme" name="content"
                                rows="3" maxlength="1000" placeholder="Chia sẻ cảm nhận của bạn (tối đa 1000 ký tự)..."
                                required></textarea>
                            <button class="tvhay-btn-gold" type="submit">
                                Gửi bình luận
                            </button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <p class="mb-4" style="color:var(--tvhay-text-muted);">
                            Vui lòng
                            <a href="${pageContext.request.contextPath}/login"
                                style="color:var(--tvhay-primary); font-weight:700;">đăng nhập</a>
                            để gửi bình luận và đánh giá.
                        </p>
                    </c:otherwise>
                </c:choose>

                <div class="comment-list">
                    <c:forEach items="${comments}" var="comment">
                        <article class="tvhay-detail-card mb-3">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <div>
                                    <strong style="color:var(--tvhay-text);">
                                        <c:out value="${comment.username}" />
                                    </strong>
                                    <c:if test="${not empty comment.rating}">
                                        <span class="ms-2 fw-bold" style="color:var(--tvhay-primary);">${comment.rating}
                                            ★</span>
                                    </c:if>
                                </div>
                                <c:if
                                    test="${(sessionScope.loggedInUser != null and sessionScope.loggedInUser.userId eq comment.userId) or (sessionScope.user != null and sessionScope.user.userId eq comment.userId)}">
                                    <form action="${pageContext.request.contextPath}/comment" method="post"
                                        class="d-inline"
                                        onsubmit="return confirm('Bạn có chắc chắn muốn xóa bình luận này?');">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="commentId" value="${comment.commentId}" />
                                        <input type="hidden" name="movieId"
                                            value="${not empty movie.id ? movie.id : movie.movieId}" />
                                        <input type="hidden" name="csrfToken"
                                            value="${not empty sessionScope.csrfToken ? sessionScope.csrfToken : pageContext.session.id}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-2"
                                            style="font-size: 0.8rem;">
                                            Xóa
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                            <p class="mb-1" style="color:var(--tvhay-text-muted);">
                                <c:out value="${comment.content}" />
                            </p>
                            <div class="d-flex justify-content-between align-items-center">
                                <small style="color:var(--tvhay-outline);">${comment.createdAt}</small>
                                <c:if test="${not empty sessionScope.loggedInUser or not empty sessionScope.user}">
                                    <button type="button" class="btn btn-sm btn-link text-decoration-none p-0 ms-3"
                                        style="color:var(--tvhay-primary);"
                                        onclick="const f = document.getElementById('reply-form-${comment.commentId}'); if(f) f.classList.toggle('d-none');">
                                        Trả lời
                                    </button>
                                </c:if>
                            </div>

                            <form id="reply-form-${comment.commentId}"
                                action="${pageContext.request.contextPath}/comment" method="post"
                                class="mt-2 ms-4 d-none">
                                <input type="hidden" name="action" value="reply" />
                                <input type="hidden" name="movieId"
                                    value="${not empty movie.id ? movie.id : movie.movieId}" />
                                <input type="hidden" name="parentId" value="${comment.commentId}" />
                                <input type="hidden" name="csrfToken"
                                    value="${not empty sessionScope.csrfToken ? sessionScope.csrfToken : pageContext.session.id}" />
                                <div class="input-group input-group-sm">
                                    <input type="text" name="content" class="form-control tvhay-input-theme"
                                        placeholder="Viết phản hồi (tối đa 1000 ký tự)..." maxlength="1000" required />
                                    <button class="tvhay-btn-gold btn-sm" type="submit">
                                        Gửi
                                    </button>
                                </div>
                            </form>

                            <c:if test="${not empty comment.replies}">
                                <div class="comment-replies ms-4 mt-3 ps-3"
                                    style="border-left: 2px solid var(--tvhay-primary);">
                                    <c:forEach items="${comment.replies}" var="reply">
                                        <div class="reply-item py-1">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <strong style="color:var(--tvhay-text);">
                                                    <c:out value="${reply.username}" />
                                                </strong>
                                                <c:if
                                                    test="${(sessionScope.loggedInUser != null and sessionScope.loggedInUser.userId eq reply.userId) or (sessionScope.user != null and sessionScope.user.userId eq reply.userId)}">
                                                    <form action="${pageContext.request.contextPath}/comment"
                                                        method="post" class="d-inline"
                                                        onsubmit="return confirm('Bạn có chắc chắn muốn xóa phản hồi này?');">
                                                        <input type="hidden" name="action" value="delete" />
                                                        <input type="hidden" name="commentId"
                                                            value="${reply.commentId}" />
                                                        <input type="hidden" name="movieId"
                                                            value="${not empty movie.id ? movie.id : movie.movieId}" />
                                                        <input type="hidden" name="csrfToken"
                                                            value="${not empty sessionScope.csrfToken ? sessionScope.csrfToken : pageContext.session.id}" />
                                                        <button type="submit"
                                                            class="btn btn-sm btn-outline-danger py-0 px-1"
                                                            style="font-size: 0.75rem;">
                                                            Xóa
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                            <p class="mb-0" style="color:var(--tvhay-text-muted);">
                                                <c:out value="${reply.content}" />
                                            </p>
                                            <small style="color:var(--tvhay-outline); font-size: 0.75rem;">
                                                ${reply.createdAt}
                                            </small>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </article>
                    </c:forEach>

                    <c:if test="${empty comments}">
                        <p class="empty-state">
                            Chưa có bình luận. Hãy là người đầu tiên chia sẻ cảm nhận.
                        </p>
                    </c:if>
                </div>
            </section>
        </div>
    </main>

    <%-- MODAL XEM TRAILER TRỰC TIẾP TRÊN TRANG --%>
    <c:if test="${not empty trailerEmbedUrl}">
        <div id="trailerModal"
            class="position-fixed top-0 start-0 w-100 h-100 d-none align-items-center justify-content-center"
            style="background:rgba(0,0,0,0.85); z-index:2000; padding:20px;"
            onclick="if(event.target === this) closeTrailerModal();">
            <div class="w-100"
                style="max-width:960px; background:#111319; border-radius:14px; overflow:hidden; border:1px solid rgba(255,198,101,0.3);">
                <div class="d-flex align-items-center justify-content-between px-3 py-2" style="background:#191b22;">
                    <strong style="color:#ffc665;">Trailer:
                        <c:out value="${movie.title}" />
                    </strong>
                    <button type="button" class="btn btn-sm btn-outline-light" onclick="closeTrailerModal()">
                        Đóng ✕
                    </button>
                </div>
                <div style="aspect-ratio:16/9; width:100%; background:#000;">
                    <iframe id="trailerIframe" data-src="${fn:escapeXml(trailerEmbedUrl)}" src="about:blank"
                        style="width:100%; height:100%; border:0;"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        allowfullscreen></iframe>
                </div>
            </div>
        </div>
        <script>
            function openTrailerModal() {
                const modal = document.getElementById("trailerModal");
                const iframe = document.getElementById("trailerIframe");
                if (modal && iframe) {
                    iframe.src = iframe.dataset.src;
                    modal.classList.remove("d-none");
                    modal.classList.add("d-flex");
                }
            }

            function closeTrailerModal() {
                const modal = document.getElementById("trailerModal");
                const iframe = document.getElementById("trailerIframe");
                if (modal && iframe) {
                    iframe.src = "about:blank";
                    modal.classList.add("d-none");
                    modal.classList.remove("d-flex");
                }
            }

        </script>
    </c:if>

    <jsp:include page="/WEB-INF/include/footer.jsp" />
    <script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>

</html>
