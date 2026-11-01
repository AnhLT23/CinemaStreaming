package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(
    urlPatterns = {
        "/about",
        "/terms",
        "/privacy",
        "/faq",
        "/sitemap",
        "/contact",
    }
)
public class InfoPageController extends HttpServlet {

    private static final Map<String, String> TITLES = Map.of(
        "/about",
        "Giới thiệu TVHAY+",
        "/terms",
        "Điều khoản sử dụng",
        "/privacy",
        "Chính sách quyền riêng tư",
        "/faq",
        "Câu hỏi thường gặp",
        "/sitemap",
        "Sơ đồ trang",
        "/contact",
        "Liên hệ"
    );

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        String path = request.getServletPath();
        request.setAttribute("pageKey", path.substring(1));
        request.setAttribute("pageTitle", TITLES.get(path));
        request
            .getRequestDispatcher("/WEB-INF/views/common/info.jsp")
            .forward(request, response);
    }
}
