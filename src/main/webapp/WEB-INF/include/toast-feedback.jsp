<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty sessionScope.successMessage or not empty sessionScope.errorMessage}">
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1080;" aria-live="polite"
        aria-atomic="true">
        <div class="toast show border-0 shadow-lg ${not empty sessionScope.errorMessage ? 'text-bg-danger' : 'text-bg-success'}"
            role="status">
            <div class="d-flex align-items-center">
                <div class="toast-body fw-semibold">
                    <c:out
                        value="${not empty sessionScope.errorMessage ? sessionScope.errorMessage : sessionScope.successMessage}" />
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"
                    aria-label="Đóng"></button>
            </div>
        </div>
    </div>
    <c:remove var="successMessage" scope="session" />
    <c:remove var="errorMessage" scope="session" />
</c:if>
