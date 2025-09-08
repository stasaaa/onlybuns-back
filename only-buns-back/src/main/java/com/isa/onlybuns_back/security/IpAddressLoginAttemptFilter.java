package com.isa.onlybuns_back.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class IpAddressLoginAttemptFilter implements Filter {

    private final LoginAttemptService loginAttemptService;

    public IpAddressLoginAttemptFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ipAddress = httpRequest.getRemoteAddr();

        if (loginAttemptService.isBlocked(ipAddress)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("Too many login attempts. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}