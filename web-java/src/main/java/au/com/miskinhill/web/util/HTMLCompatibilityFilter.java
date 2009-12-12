package au.com.miskinhill.web.util;

import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

@Component("htmlCompatibilityFilter")
public class HTMLCompatibilityFilter extends HttpResponseBufferingFilter {
    
    /* XXX lame */
    
    private static final Pattern SELF_CLOSING_SCRIPT_PATTERN = Pattern.compile("<script(\\s[^>]*?)?\\s*/>");
    private static final Pattern SELF_CLOSING_SPAN_PATTERN = Pattern.compile("<span(\\s[^>]*?)?\\s*/>");
    private static final Pattern SELF_CLOSING_ABBR_PATTERN = Pattern.compile("<abbr(\\s[^>]*?)?\\s*/>");
    private static final Pattern SELF_CLOSING_A_PATTERN = Pattern.compile("<a(\\s[^>]*?)?\\s*/>");
    
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
        responseBody = SELF_CLOSING_SCRIPT_PATTERN.matcher(responseBody).replaceAll("<script$1></script>");
        responseBody = SELF_CLOSING_SPAN_PATTERN.matcher(responseBody).replaceAll("<span$1></span>");
        responseBody = SELF_CLOSING_ABBR_PATTERN.matcher(responseBody).replaceAll("<abbr$1></abbr>");
        responseBody = SELF_CLOSING_A_PATTERN.matcher(responseBody).replaceAll("<a$1></a>");
        return responseBody;
    };

}
