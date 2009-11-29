package au.com.miskinhill.web.util;

import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

@Component("linkRelativizingFilter")
public class LinkRelativizingFilter extends HttpResponseBufferingFilter {
    
    /*
     * I would do this with a real XML parser, but apparently there are none for Java that actually work properly :-(
     */
    
    private static final String ABSOLUTE_PREFIX = "http://miskinhill.com.au";
    private static final Pattern PATTERN = Pattern.compile("((?:href|src)=['\"])" + Pattern.quote(ABSOLUTE_PREFIX) + "([^'\"]*['\"])");
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    protected boolean shouldBuffer(HttpServletRequest request) {
        return true;
    }
    
    @Override
    protected boolean shouldPostprocessResponse(String contentType, int status) {
        return MediaType.TEXT_HTML_TYPE.isCompatible(MediaType.valueOf(contentType)) &&
                status == 200; // XXX 206 and suchlike?
    }
    
    @Override
    protected String postprocessResponse(String responseBody) {
        return PATTERN.matcher(responseBody).replaceAll("$1$2");
    };

}
