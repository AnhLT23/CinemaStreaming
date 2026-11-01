package ultil;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Owner: Tuấn Anh / Hữu Lộc.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/FileUpload.java.
 */
public final class FileUpload {

    /**
     * Thư mục lưu ảnh tĩnh trên máy chủ. Có thể ghi đè bằng -Dcinema.upload.dir
     * hoặc biến môi trường CINEMA_UPLOAD_DIR.
     */
    public static final Path UPLOAD_DIR = Path.of(
        System.getProperty(
            "cinema.upload.dir",
            System.getenv("CINEMA_UPLOAD_DIR") != null
                ? System.getenv("CINEMA_UPLOAD_DIR")
                : System.getProperty("user.home") + "/CinemaStreamingUploads"
        )
    )
        .toAbsolutePath()
        .normalize();

    private FileUpload() {}

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: perform the save operation. */
    public static Path save(InputStream input, Path directory, String fileName)
        throws IOException {
        if (
            input == null ||
            directory == null ||
            fileName == null ||
            fileName.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Input, destination directory, and file name are required."
            );
        }
        Path root = directory.toAbsolutePath().normalize();
        Path destination = root
            .resolve(Path.of(fileName).getFileName())
            .normalize();
        if (!destination.startsWith(root)) {
            throw new IllegalArgumentException(
                "File name resolves outside the destination directory."
            );
        }
        Files.createDirectories(root);
        try (InputStream source = input) {
            Files.copy(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
        return destination;
    }

    /** Owner: Tuấn Anh / Hữu Lộc. Purpose: save image. */
    public static String saveImage(Part part, String realPath)
        throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String submittedFileName = part.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.isBlank()) {
            throw new IllegalArgumentException("Tên file ảnh không hợp lệ.");
        }
        String submitted = Paths.get(submittedFileName)
            .getFileName()
            .toString();
        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot >= 0) {
            ext = submitted.substring(dot).toLowerCase(Locale.ROOT);
        }
        if (
            !(
                ".jpg".equals(ext) ||
                ".jpeg".equals(ext) ||
                ".png".equals(ext) ||
                ".webp".equals(ext)
            )
        ) {
            throw new IllegalArgumentException(
                "Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP."
            );
        }
        String contentType = part.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                "File tải lên không phải hình ảnh."
            );
        }
        Path folder = UPLOAD_DIR.resolve("uploads").normalize();
        Files.createDirectories(folder);
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        save(part.getInputStream(), folder, name);
        return "uploads/" + name;
    }
}
