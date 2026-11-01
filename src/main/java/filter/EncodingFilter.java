package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Owner: Lộc / Toàn nhóm.
 * Role: Servlet filter for request encoding, authentication, or session policy.
 * Source: src/main/java/filter/EncodingFilter.java.
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    /** Owner: Lộc / Toàn nhóm. Purpose: initialize servlet or filter state. */
    @Override
    public void init(FilterConfig filterConfig) {}

    /** Owner: Lộc / Toàn nhóm. Purpose: filter and authorize the incoming request before dispatch. */
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    /** Owner: Lộc / Toàn nhóm. Purpose: release servlet or filter resources. */
    @Override
    public void destroy() {}
}
