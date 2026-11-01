<%-- File: /WEB-INF/include/pagination.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="currentPage"
    value="${not empty requestScope.currentPage ? requestScope.currentPage : (empty param.page ? 1 : param.page)}" />
<c:set var="totalPages" value="${empty requestScope.totalPages ? 1 : requestScope.totalPages}" />
<c:set var="filterParams" value="" />
<c:if test="${not empty param.q}">
    <c:set var="filterParams" value="${filterParams}&amp;q=${param.q}" />
</c:if>
<c:if test="${empty param.q and not empty requestScope.q}">
    <c:set var="filterParams" value="${filterParams}&amp;q=${requestScope.q}" />
</c:if>
<c:if test="${not empty param.genreId}">
    <c:set var="filterParams" value="${filterParams}&amp;genreId=${param.genreId}" />
</c:if>
<c:if test="${empty param.genreId and not empty requestScope.selectedGenreId}">
    <c:set var="filterParams" value="${filterParams}&amp;genreId=${requestScope.selectedGenreId}" />
</c:if>
<c:if test="${empty param.genreId and empty requestScope.selectedGenreId and not empty param.genre}">
    <c:set var="filterParams" value="${filterParams}&amp;genre=${param.genre}" />
</c:if>
<c:if test="${not empty param.type}">
    <c:set var="filterParams" value="${filterParams}&amp;type=${param.type}" />
</c:if>
<c:if test="${not empty param.year}">
    <c:set var="filterParams" value="${filterParams}&amp;year=${param.year}" />
</c:if>

<c:if test="${totalPages gt 1}">
    <style>
        .tvhay-pagination {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 8px;
            list-style: none;
            padding: 0;
            margin: 36px 0 0;
        }

        .tvhay-page-link {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 40px;
            height: 40px;
            padding: 0 14px;
            border-radius: 10px;
            background: var(--tvhay-surface);
            color: var(--tvhay-text-muted);
            font-size: 13px;
            font-weight: 700;
            text-decoration: none;
            border: 1px solid var(--tvhay-border);
            transition: all 0.2s ease;
        }

        .tvhay-page-link:hover {
            background: var(--tvhay-surface-high);
            color: var(--tvhay-primary);
        }

        .tvhay-page-item.active .tvhay-page-link {
            background: #ffc665;
            color: #432c00;
            box-shadow: 0 0 14px rgba(229, 169, 60, 0.4);
            border-color: #ffc665;
        }

        .tvhay-page-item.disabled .tvhay-page-link {
            opacity: 0.4;
            pointer-events: none;
        }

    </style>
    <nav aria-label="Điều hướng trang phim">
        <ul class="tvhay-pagination">
            <li class="tvhay-page-item ${currentPage le 1 ? 'disabled' : ''}">
                <a class="tvhay-page-link" href="?page=${currentPage - 1}&amp;pageSize=20${filterParams}"
                    aria-label="Trang trước">‹</a>
            </li>
            <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                <li class="tvhay-page-item ${pageNumber eq currentPage ? 'active' : ''}">
                    <a class="tvhay-page-link"
                        href="?page=${pageNumber}&amp;pageSize=20${filterParams}">${pageNumber}</a>
                </li>
            </c:forEach>
            <li class="tvhay-page-item ${currentPage ge totalPages ? 'disabled' : ''}">
                <a class="tvhay-page-link" href="?page=${currentPage + 1}&amp;pageSize=20${filterParams}"
                    aria-label="Trang sau">›</a>
            </li>
        </ul>
    </nav>
</c:if>
