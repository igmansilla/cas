package com.cas.login.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CookieLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not needed for this filter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Proceed with the filter chain first, so cookies are set by subsequent filters/handlers
        chain.doFilter(request, response);

        // Log cookies after the response is processed by the chain
        // but before it's sent to the client
        for (Cookie cookie : httpServletResponse.getCookies()) {
            System.out.println(
                String.format("Responding with cookie: [%s] = [%s]; HttpOnly=[%b]; Path=[%s]",
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.isHttpOnly(),
                    cookie.getPath()
                )
            );
        }
    }

    @Override
    public void destroy() {
        // Not needed for this filter
    }
}
