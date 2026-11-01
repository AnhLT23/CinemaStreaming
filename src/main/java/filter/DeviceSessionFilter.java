package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Owner: Tuấn Anh.
 * Role: Servlet filter for request encoding, authentication, or session policy.
 * Source: src/main/java/filter/DeviceSessionFilter.java.
 */
@WebFilter({ "/watch", "/watch/*", "/xem-phim" })
public class DeviceSessionFilter implements Filter {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /** Owner: Tuấn Anh. Purpose: initialize servlet or filter state. */
    @Override
    public void init(FilterConfig filterConfig) {}

    /** Owner: Tuấn Anh. Purpose: filter and authorize the incoming request before dispatch. */
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) request).getSession(false);
        Object allowed =
            session == null
                ? null
                : session.getAttribute("streamSessionAllowed");
        if (Boolean.FALSE.equals(allowed)) {
            ((HttpServletResponse) response).sendError(
                HTTP_TOO_MANY_REQUESTS,
                "The account's concurrent stream limit has been reached."
            );
            return;
        }
        chain.doFilter(request, response);
    }

    /** Owner: Tuấn Anh. Purpose: release servlet or filter resources. */
    @Override
    public void destroy() {}
}
