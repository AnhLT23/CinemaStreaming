<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="card" value="${requestScope.cardMovie}" />

<article class="tvhay-card">
    <a class="tvhay-card-link" href="${pageContext.request.contextPath}/movie?id=${card.id}">
        <div class="tvhay-card-poster">
            <c:choose>
                <c:when test="${not empty card.posterPath}">
                    <c:set var="posterSrc"
                        value="${card.posterPath.startsWith('http') ? card.posterPath : (card.posterPath.startsWith('/') ? pageContext.request.contextPath.concat(card.posterPath) : pageContext.request.contextPath.concat('/images/').concat(card.posterPath))}" />
                    <img src="${posterSrc}" alt="${card.title}" loading="lazy"
                        onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/img/poster-sample.svg'" />
                </c:when>
                <c:otherwise>
                    <img src="${pageContext.request.contextPath}/assets/img/poster-sample.svg" alt="${card.title}"
                        loading="lazy" />
                </c:otherwise>
            </c:choose>

            <span class="tvhay-poster-scrim"></span>

            <%-- Nhãn chất lượng HD / 4K / VIP góc trên phải --%>
            <span class="tvhay-badge-top-right">
                <c:choose>
                    <c:when test="${card.vipOnly}">VIP</c:when>
                    <c:when test="${not empty card.quality}">
                        <c:out value="${card.quality}" />
                    </c:when>
                    <c:otherwise>HD</c:otherwise>
                </c:choose>
            </span>

            <%-- Nút Play tròn màu vàng hiện lên khi Hover --%>
            <div class="tvhay-hover-overlay">
                <div class="tvhay-play-circle">
                    <span class="material-symbols-outlined"
                        style="font-size:28px; font-variation-settings:'FILL' 1;">play_arrow</span>
                </div>
            </div>
        </div>

        <%-- Thông tin bên dưới Poster theo đúng bố cục home_2.html --%>
        <div class="tvhay-card-caption">
            <h3 class="tvhay-card-title" title="${card.title}">
                <c:out value="${card.title}" />
            </h3>
            <div class="tvhay-card-sub">
                <span class="tvhay-card-audio">
                    <c:out value="${not empty card.audio ? card.audio : 'HD Thuyết minh'}" />
                </span>
                <span class="tvhay-card-year">
                    <c:out value="${not empty card.releaseYear ? card.releaseYear : 'Full'}" />
                </span>
            </div>
            <div class="tvhay-card-views">
                <span class="material-symbols-outlined" style="font-size:14px;">visibility</span>
                <span>HD •
                    <c:out value="${not empty card.releaseYear ? card.releaseYear : '2025'}" />
                </span>
            </div>
        </div>
    </a>
</article>
