<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi" data-theme="${sessionScope.theme}">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý hội viên | Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sign-in.css" />
    <meta name="csrf-token" content="${sessionScope.csrfToken}" />
</head>

<body>
    <main class="main-card">
        <h1>Quản lý hội viên</h1>
        <c:if test="${param.message eq 'updated'}">
            <p class="success">Cập nhật trạng thái thành công.</p>
        </c:if>
        <form action="${pageContext.request.contextPath}/admin/members" method="get" class="toolbar">
            <label>Tìm theo tên/email<input type="search" name="q" value="${fn:escapeXml(q)}"
                    maxlength="60" /></label>
            <label>Trạng thái<select name="status">
                    <option value="ALL" ${statusFilter eq 'ALL' ? 'selected' : '' }>
                        Tất cả
                    </option>
                    <option value="ACTIVE" ${statusFilter eq 'ACTIVE' ? 'selected' : '' }>
                        Hoạt động
                    </option>
                    <option value="LOCKED" ${statusFilter eq 'LOCKED' ? 'selected' : '' }>
                        Đã khóa
                    </option>
                </select></label>
            <button type="submit">Lọc</button>
        </form>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Mã hội viên</th>
                        <th>Tên</th>
                        <th>Email (đã che)</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${members}" var="u">
                        <tr>
                            <td>
                                <c:out value="${u.userCode}" />
                            </td>
                            <td>
                                <c:out value="${u.fullName}" /> (
                                <c:out value="${u.username}" />)
                            </td>
                            <td>
                                <c:out value="${u.maskedEmail}" />
                            </td>
                            <td>
                                <c:out value="${u.role}" />
                            </td>
                            <td>
                                <c:out value="${u.status}" />
                            </td>
                            <td>
                                <c:if test="${u.role eq 'CUSTOMER'}">
                                    <form action="${pageContext.request.contextPath}/admin/members" method="post">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" /><input
                                            type="hidden" name="action" value="toggle_status" />
                                        <input type="hidden" name="userId" value="${u.id}" />
                                        <button type="submit">
                                            <c:choose>
                                                <c:when test="${u.status eq 'ACTIVE'}">Khóa</c:when>
                                                <c:otherwise>Mở khóa</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <p>Trang
            <c:out value="${pageNumber}" /> — tối đa 20 tài khoản/trang
        </p>
        <c:url var="previousPageUrl" value="/admin/members">
            <c:param name="page" value="${pageNumber-1}" />
            <c:param name="q" value="${q}" />
            <c:param name="status" value="${statusFilter}" />
        </c:url>
        <c:url var="nextPageUrl" value="/admin/members">
            <c:param name="page" value="${pageNumber+1}" />
            <c:param name="q" value="${q}" />
            <c:param name="status" value="${statusFilter}" />
        </c:url>
        <nav>
            <c:if test="${pageNumber gt 1}"><a href="${pageContext.request.contextPath}${previousPageUrl}">Trang
                    trước</a></c:if>
            <c:if test="${not empty members and members.size() eq 20}"><a
                    href="${pageContext.request.contextPath}${nextPageUrl}">Trang sau</a></c:if>
        </nav>
        <p>
            <a href="${pageContext.request.contextPath}/account">Về tài khoản</a>
        </p>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/theme.js" defer></script>
</body>

</html>
