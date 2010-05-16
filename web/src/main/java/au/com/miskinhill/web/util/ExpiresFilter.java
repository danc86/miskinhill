package au.com.miskinhill.web.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class ExpiresFilter extends GenericFilterBean {
    
    private int expiryHours;
    
    public void setExpiryHours(int expiryHours) {
        this.expiryHours = expiryHours;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).addDateHeader("Expires",
                    System.currentTimeMillis() + expiryHours * 60 * 60 * 1000);
        }
        chain.doFilter(request, response);
    }

}
