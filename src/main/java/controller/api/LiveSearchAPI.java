package controller.api;

import dao.MovieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Movie;

/**
 * Owner: Chuẩn.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/api/LiveSearchAPI.java.
 */
@WebServlet(
    name = "LiveSearchAPI",
    urlPatterns = { "/api/movies/quick-search", "/api/search" }
)
public class LiveSearchAPI extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
        LiveSearchAPI.class.getName()
    );
    private MovieDAO movieDAO;

    /** Owner: Chuẩn. Purpose: initialize servlet or filter state. */
    @Override
    public void init() throws ServletException {
        super.init();
        if (this.movieDAO == null) {
            this.movieDAO = new MovieDAO();
        }
    }

    /** Owner: Chuẩn. Purpose: set movie dao. */
    public void setMovieDAO(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(
            "Cache-Control",
            "no-cache, no-store, must-revalidate"
        );

        PrintWriter out = response.getWriter();

        try {
            String q = request.getParameter("q");

            if (q == null) {
                out.write("[]");
                return;
            }

            q = q.trim();
            if (q.isEmpty()) {
                out.write("[]");
                return;
            }

            // Giới hạn độ dài từ khóa tối đa 100 ký tự để tránh lạm dụng bộ nhớ
            if (q.length() > 100) {
                q = q.substring(0, 100);
            }

            List<Movie> movies = movieDAO.searchQuick(q);
            if (movies == null) {
                movies = Collections.emptyList();
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{")
                    .append("\"id\":")
                    .append(m.getMovieId())
                    .append(",")
                    .append("\"title\":\"")
                    .append(escapeJson(m.getTitle()))
                    .append("\",")
                    .append("\"posterUrl\":\"")
                    .append(escapeJson(m.getPosterUrl()))
                    .append("\",")
                    .append("\"avgRating\":")
                    .append(String.format(Locale.US, "%.1f", m.getAvgRating()))
                    .append("}");
            }
            json.append("]");

            out.write(json.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý LiveSearchAPI", e);
            // Tuyệt đối không làm lộ stack trace hoặc thông tin database ra client
            out.write("[]");
        }
    }

    /**
     * Escape an toàn chuỗi ký tự theo chuẩn JSON RFC 8259, chống XSS và vỡ cấu trúc JSON.
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}
