package au.com.miskinhill.web.util;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sitemesh.webapp.contentfilter.Selector;

import org.sitemesh.webapp.contentfilter.BasicSelector;
import org.sitemesh.webapp.contentfilter.ContentBufferingFilter;
import org.sitemesh.webapp.contentfilter.ResponseMetaData;

public class LinkRelativizingFilter extends ContentBufferingFilter {
    
    /*
     * I would do this with a real XML parser, but apparently there are none for Java that actually work properly :-(
     */
    
    private static final String ABSOLUTE_PREFIX = "http://miskinhill.com.au";
    private static final Pattern PATTERN = Pattern.compile("((?:href|src)=['\"])" + Pattern.quote(ABSOLUTE_PREFIX) + "([^'\"]*['\"])");
    private static final Selector SELECTOR = new BasicSelector("text/html") {
        protected boolean filterAlreadyAppliedForRequest(HttpServletRequest request) {
            return false; // dispatcher=REQUEST in web.xml handles this for us
        };
    };

    public LinkRelativizingFilter() {
        super(SELECTOR);
    }

    @Override
    protected boolean postProcess(String contentType, CharBuffer buffer,
            HttpServletRequest request, HttpServletResponse response,
            ResponseMetaData responseMetaData) throws IOException, ServletException {
        response.getWriter().write(PATTERN.matcher(buffer).replaceAll("$1$2"));
        return true;
    }

}
