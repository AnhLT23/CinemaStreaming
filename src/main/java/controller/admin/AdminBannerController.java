package controller.admin;

import dao.BannerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import model.Banner;
import ultil.FileUpload;
import ultil.SecurityUtil;

/**
 * Owner: Hữu Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/admin/AdminBannerController.java.
 */
@WebServlet("/admin/banners")
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 6291456)
public class AdminBannerController extends HttpServlet {

    private final BannerDAO dao = new BannerDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            SecurityUtil.csrf(req);
            String id = req.getParameter("edit");
            Banner banner = null;
            if (id != null && !id.trim().isEmpty()) {
                int bannerId = parsePositiveInt(id, "Banner ID không hợp lệ.");
                banner = dao.getById(bannerId);
                if (banner == null) {
                    resp.sendError(404);
                    return;
                }
            } else {
                banner = new Banner();
                LocalDateTime now = LocalDateTime.now()
                    .withSecond(0)
                    .withNano(0);
                banner.setStartDate(Timestamp.valueOf(now));
                banner.setEndDate(Timestamp.valueOf(now.plusMonths(1)));
                banner.setActive(true);
            }
            req.setAttribute("editBanner", banner);
            req.setAttribute("banners", dao.getAll());
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/banner-manage.jsp"
            ).forward(req, resp);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "CSRF không hợp lệ"
            );
            return;
        }
        Banner banner = new Banner();
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                int bannerId = parsePositiveInt(
                    req.getParameter("bannerId"),
                    "Banner ID không hợp lệ."
                );
                if (!dao.delete(bannerId)) {
                    resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy banner."
                    );
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/admin/banners");
                return;
            }
            if (!"save".equals(action)) {
                resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Action không hợp lệ."
                );
                return;
            }
            String id = req.getParameter("bannerId");
            if (id != null && !id.trim().isEmpty()) {
                banner.setBannerId(
                    parsePositiveInt(id, "Banner ID không hợp lệ.")
                );
            }
            Banner existing =
                banner.getBannerId() > 0
                    ? dao.getById(banner.getBannerId())
                    : null;
            if (banner.getBannerId() > 0 && existing == null) {
                resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy banner."
                );
                return;
            }
            banner.setTitle(parameter(req, "title"));
            banner.setTargetUrl(parameter(req, "targetUrl"));
            banner.setPosition(parameter(req, "position"));
            String mid = req.getParameter("movieId");
            banner.setMovieId(
                mid == null || mid.trim().isEmpty()
                    ? null
                    : parsePositiveInt(mid, "Movie ID không hợp lệ.")
            );
            banner.setStartDate(
                parseDateTime(
                    req.getParameter("startDate"),
                    "Ngày bắt đầu không hợp lệ."
                )
            );
            banner.setEndDate(
                parseDateTime(
                    req.getParameter("endDate"),
                    "Ngày kết thúc không hợp lệ."
                )
            );
            if (
                banner.getTitle().isEmpty() ||
                banner.getTitle().length() > 200 ||
                banner.getTargetUrl().isEmpty() ||
                banner.getTargetUrl().length() > 500 ||
                banner.getPosition().isEmpty() ||
                banner.getPosition().length() > 50 ||
                banner.getStartDate().after(banner.getEndDate()) ||
                !isSafeTargetUrl(banner.getTargetUrl())
            ) {
                throw new IllegalArgumentException(
                    "Dữ liệu banner không hợp lệ hoặc ngày bắt đầu lớn hơn ngày kết thúc."
                );
            }
            String image = FileUpload.saveImage(
                req.getPart("imageFile"),
                getServletContext().getRealPath("/")
            );
            banner.setImageUrl(
                image != null
                    ? image
                    : existing == null
                      ? null
                      : existing.getImageUrl()
            );
            if (
                banner.getImageUrl() == null ||
                banner.getImageUrl().trim().isEmpty()
            ) {
                throw new IllegalArgumentException("Vui lòng chọn ảnh banner.");
            }
            banner.setActive(req.getParameter("isActive") != null);
            if (banner.getBannerId() > 0) {
                dao.update(banner);
            } else {
                dao.insert(banner);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/banners");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("editBanner", banner);
            SecurityUtil.csrf(req);
            try {
                req.setAttribute("banners", dao.getAll());
            } catch (SQLException ex) {
                throw new ServletException(
                    "Không thể tải danh sách banner",
                    ex
                );
            }
            req.getRequestDispatcher(
                "/WEB-INF/views/admin/banner-manage.jsp"
            ).forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static String parameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static int parsePositiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(message);
    }

    private static Timestamp parseDateTime(String value, String message) {
        try {
            return Timestamp.valueOf(LocalDateTime.parse(value));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(message, ex);
        }
    }

    private static boolean isSafeTargetUrl(String value) {
        if (value.startsWith("/") && !value.startsWith("//")) {
            return true;
        }
        try {
            URI uri = new URI(value);
            return (
                uri.isAbsolute() &&
                ("http".equalsIgnoreCase(uri.getScheme()) ||
                    "https".equalsIgnoreCase(uri.getScheme()))
            );
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
