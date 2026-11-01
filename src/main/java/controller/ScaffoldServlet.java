package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Owner: Hữu Lộc / Toàn nhóm.
 * Role: HTTP controller/servlet that coordinates requests, data access, and views.
 * Source: src/main/java/controller/ScaffoldServlet.java.
 */
public abstract class ScaffoldServlet extends HttpServlet {

    @Override
    protected void service(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        response.sendError(
            HttpServletResponse.SC_NOT_IMPLEMENTED,
            getServletName() + " endpoint has not been implemented yet."
        );
    }
}
