<%-- File: /WEB-INF/include/footer.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<style>
    .tvhay-footer {
        width: 100%;
        background-color: var(--tvhay-surface-lowest);
        color: var(--tvhay-text);
        padding: 48px 0 32px;
        margin-top: auto;
        border-top: 1px solid var(--tvhay-border);
        transition: background-color 0.25s ease, color 0.25s ease;
    }

    .tvhay-footer-container {
        max-width: 1680px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .tvhay-footer-grid {
        display: grid;
        grid-template-columns: 2fr 1fr 1fr 1fr;
        gap: 32px;
        margin-bottom: 36px;
    }

    @media (max-width: 991.98px) {
        .tvhay-footer-grid {
            grid-template-columns: 1fr 1fr;
        }
    }

    @media (max-width: 575.98px) {
        .tvhay-footer-grid {
            grid-template-columns: 1fr;
        }
    }

    .tvhay-footer-desc {
        color: var(--tvhay-text-muted);
        font-size: 13px;
        line-height: 1.65;
        max-width: 380px;
        margin: 12px 0 14px;
    }

    .tvhay-footer-brand {
        display: inline-flex;
        align-items: center;
        line-height: 0;
        text-decoration: none;
    }

    .tvhay-footer-brand img {
        display: block;
        width: 140px;
        height: 36px;
        object-fit: contain;
        transition: opacity 0.2s ease;
    }

    .tvhay-footer-brand:hover img {
        opacity: 0.86;
    }

    .tvhay-footer-badges {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .tvhay-footer-badge {
        padding: 4px 8px;
        border-radius: 4px;
        background: var(--tvhay-surface);
        color: var(--tvhay-primary);
        border: 1px solid var(--tvhay-border);
        font-size: 10px;
        font-weight: 800;
        letter-spacing: 0.08em;
    }

    .tvhay-footer-col-title {
        font-size: 15px;
        font-weight: 700;
        color: var(--tvhay-text);
        margin-bottom: 12px;
        display: block;
    }

    .tvhay-footer-links {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .tvhay-footer-links a {
        color: var(--tvhay-text-muted);
        font-size: 13px;
        text-decoration: none;
        transition: color 0.2s ease;
    }

    .tvhay-footer-links a:hover {
        color: var(--tvhay-primary);
    }

    .tvhay-footer-bottom {
        padding-top: 24px;
        border-top: 1px solid var(--tvhay-border);
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        font-size: 12px;
        color: var(--tvhay-outline);
    }

</style>

<footer class="tvhay-footer">
    <div class="tvhay-footer-container">
        <div class="tvhay-footer-grid">
            <div>
                <a class="tvhay-footer-brand" href="${ctx}/home" aria-label="TVHAY+ trang chủ">
                    <img src="${ctx}/assets/img/cinema-logo.svg" alt="TVHAY+" />
                </a>
                <p class="tvhay-footer-desc">
                    TVHAY+ ORIGINAL - Nền tảng xem phim trực tuyến độ nét cao
                    với kho phim bom tấn, phim bộ, phim lẻ thuyết minh và
                    vietsub nhanh nhất với chất lượng chuẩn rạp.
                </p>
                <div class="tvhay-footer-badges">
                    <span class="tvhay-footer-badge">HD</span>
                    <span class="tvhay-footer-badge">4K UHD</span>
                    <span class="tvhay-footer-badge">DOLBY ATMOS</span>
                </div>
            </div>

            <div class="tvhay-footer-links">
                <span class="tvhay-footer-col-title">Thể Loại</span>
                <a href="${ctx}/movies?q=Hành+Động">Phim Hành Động</a>
                <a href="${ctx}/movies?q=Cổ+Trang">Phim Cổ Trang</a>
                <a href="${ctx}/movies?q=Kinh+Dị">Phim Kinh Dị</a>
                <a href="${ctx}/movies?q=Hoạt+Hình">Phim Hoạt Hình</a>
                <a href="${ctx}/movies?q=Viễn+Tưởng">Phim Viễn Tưởng</a>
            </div>

            <div class="tvhay-footer-links">
                <span class="tvhay-footer-col-title">Quốc Gia</span>
                <a href="${ctx}/movies?q=Trung+Quốc">Phim Trung Quốc</a>
                <a href="${ctx}/movies?q=Hàn+Quốc">Phim Hàn Quốc</a>
                <a href="${ctx}/movies?q=Âu+Mỹ">Phim Âu Mỹ</a>
                <a href="${ctx}/movies?q=Thái+Lan">Phim Thái Lan</a>
                <a href="${ctx}/movies?q=Nhật+Bản">Phim Nhật Bản</a>
            </div>

            <div class="tvhay-footer-links">
                <span class="tvhay-footer-col-title">Hỗ Trợ &amp; Bản Quyền</span>
                <a href="${ctx}/terms">Điều khoản chung</a>
                <a href="${ctx}/privacy">Chính sách riêng tư</a>
                <a href="${ctx}/about">Giới thiệu TVHAY+</a>
                <a href="${ctx}/faq">Câu hỏi thường gặp (FAQ)</a>
                <a href="${ctx}/sitemap">Sitemap</a>
                <a href="${ctx}/contact">Liên hệ quảng cáo</a>
            </div>
        </div>

        <div class="tvhay-footer-bottom">
            <span>Disclaimer: Trang web hoàn toàn phi lợi nhuận và phục vụ học
                tập. Mọi nội dung video được sưu tầm từ các dịch vụ chia sẻ trực
                tuyến công cộng.</span>
            <span>© 2026 TVHAY+ ORIGINAL. All rights reserved.</span>
        </div>
    </div>
</footer>
