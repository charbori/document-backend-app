package main.blog.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import main.blog.util.ApiAuthUtil;
import main.blog.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

@Slf4j
//@Component
public class ApiAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorizationKey = req.getHeader("Authorization");
        String checkUri = req.getRequestURI();

        // /api/ 도메인만 검사한다.
        if (checkUri.matches("^/api/.*") && (checkUri.matches("^/api/upload/video.*") == false)) {
            ApiAuthUtil.isApiAuthenticated(authorizationKey);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            log.info("logfilter dofilter end", req.getRequestURI());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
