package au.com.miskinhill.web.util;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenericRedirectingFilter implements Filter {
    
    private static final Logger LOG = Logger.getLogger(GenericRedirectingFilter.class.getName());
    private final Map<Pattern, String> redirects = new LinkedHashMap<Pattern, String>();
    
    @SuppressWarnings("unchecked")
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Enumeration<String> initParameterNames = filterConfig.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String pattern = initParameterNames.nextElement();
            redirects.put(Pattern.compile(pattern), filterConfig.getInitParameter(pattern));
        }
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain chain)
            throws IOException, ServletException {
        if (_request instanceof HttpServletRequest && _response instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) _request;
            HttpServletResponse response = (HttpServletResponse) _response;
            String pathInfo = getPathInfo(request);
            for (Map.Entry<Pattern, String> redirect: redirects.entrySet()) {
                Matcher matcher = redirect.getKey().matcher(pathInfo);
                if (matcher.matches()) {
                    String redirectLocation = URI.create(request.getRequestURL().toString())
                            .resolve(matcher.replaceAll(redirect.getValue())).toString();
                    LOG.info("Redirecting " + pathInfo + " to " + redirectLocation);
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.addHeader("Location", redirectLocation);
                    return;
                }
            }
        }
        chain.doFilter(_request, _response);
    }
    
    private String getPathInfo(HttpServletRequest request) {
        return request.getContextPath() + request.getServletPath() +
                (request.getPathInfo() != null ? request.getPathInfo() : "");
    }

}
