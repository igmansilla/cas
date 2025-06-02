package com.cas.login.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CookieLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Not needed for this filter
    }    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Create a wrapper to capture cookies being added to the response
        CookieCapturingResponseWrapper responseWrapper = new CookieCapturingResponseWrapper(httpServletResponse);

        // Proceed with the filter chain
        chain.doFilter(request, responseWrapper);

        // Log captured cookies
        for (Cookie cookie : responseWrapper.getCapturedCookies()) {
            System.out.println(
                String.format("Responding with cookie: [%s] = [%s]; HttpOnly=[%b]; Path=[%s]",
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.isHttpOnly(),
                    cookie.getPath()
                )
            );
        }
    }    @Override
    public void destroy() {
        // Not needed for this filter
    }
}

class CookieCapturingResponseWrapper extends HttpServletResponseWrapper {
    private final List<Cookie> capturedCookies = new ArrayList<>();

    public CookieCapturingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        capturedCookies.add(cookie);
        super.addCookie(cookie);
    }

    public List<Cookie> getCapturedCookies() {
        return new ArrayList<>(capturedCookies);
    }
}
