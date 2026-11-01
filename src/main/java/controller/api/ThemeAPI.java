package controller.api;

import dao.UserDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import ultil.CookieUtil;
import ultil.SecurityUtil;

/**
 * Owner: Lộc.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/api/ThemeAPI.java.
 */
@WebServlet("/api/theme")
public class ThemeAPI extends HttpServlet {

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        String theme = (String) req.getSession().getAttribute("theme");
        if (theme == null) theme = CookieUtil.get(req, CookieUtil.THEME_COOKIE);
        json(
            resp,
            "{\"status\":\"success\",\"theme\":\"" +
                ("dark".equals(theme) ? "dark" : "light") +
                "\"}"
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        req.setCharacterEncoding("UTF-8");
        if (!SecurityUtil.validCsrf(req)) {
            resp.sendError(403);
            return;
        }
        String theme = req.getParameter("theme");
        if (!"light".equals(theme) && !"dark".equals(theme)) {
            resp.sendError(400);
            return;
        }
        User me = (User) req.getSession().getAttribute("loggedInUser");
        if (me != null && !users.updateTheme(me.getId(), theme)) {
            resp.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể lưu giao diện"
            );
            return;
        }
        req.getSession().setAttribute("theme", theme);
        CookieUtil.set(
            req,
            resp,
            CookieUtil.THEME_COOKIE,
            theme,
            30 * 24 * 60 * 60,
            false
        );
        json(resp, "{\"status\":\"success\",\"theme\":\"" + theme + "\"}");
    }

    private void json(HttpServletResponse response, String output)
        throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(output);
    }
}
