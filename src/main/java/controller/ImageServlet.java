package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import ultil.FileUpload;

/**
 * Phục vụ ảnh tĩnh: ưu tiên thư mục upload (FileUpload.UPLOAD_DIR), dự phòng assets/img của webapp.
 * Chống path traversal bằng normalize + startsWith.
 */
/**
 * Owner: Tuấn Anh.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/ImageServlet.java.
 */
@WebServlet(name = "ImageServlet", urlPatterns = { "/image/*", "/images/*" })
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (
            pathInfo == null ||
            pathInfo.length() <= 1 ||
            pathInfo.indexOf('\0') >= 0
        ) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String relative = pathInfo.substring(1);

        Path file = resolveSafe(FileUpload.UPLOAD_DIR, relative);
        String assetsRoot = getServletContext().getRealPath("/assets/img");
        Path assetsRootPath =
            assetsRoot != null
                ? Path.of(assetsRoot).toAbsolutePath().normalize()
                : null;

        if (file == null && assetsRootPath != null) {
            file = resolveSafe(assetsRootPath, relative);
        }
        // Dự phòng ảnh mẫu nếu file ảnh trong DB chưa có trên máy
        if (file == null && assetsRootPath != null) {
            String fallbackName =
                relative.toLowerCase().contains("banner") ||
                relative.toLowerCase().contains("bg")
                    ? "banner-sample.png"
                    : "poster-sample.png";
            file = resolveSafe(assetsRootPath, fallbackName);
        }
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = getServletContext().getMimeType(
            file.getFileName().toString()
        );
        if (mime == null || !mime.startsWith("image/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(mime);
        response.setContentLengthLong(Files.size(file));
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");

        byte[] buffer = new byte[1024];
        try (
            InputStream in = Files.newInputStream(file);
            OutputStream out = response.getOutputStream()
        ) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }

    private static Path resolveSafe(Path root, String relative) {
        try {
            if (root == null) return null;
            Path candidate = root.resolve(relative).normalize();
            if (candidate.startsWith(root) && Files.isRegularFile(candidate)) {
                return candidate;
            }
        } catch (RuntimeException e) {
            // đường dẫn không hợp lệ -> coi như không tồn tại
        }
        return null;
    }
}
