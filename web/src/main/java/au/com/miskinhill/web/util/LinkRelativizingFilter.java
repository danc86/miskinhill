package au.com.miskinhill.web.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component("linkRelativizingFilter")
public class LinkRelativizingFilter extends HttpResponseBufferingFilter {
    
    /*
     * I would do this with a real XML parser, but apparently there are none for Java that actually work properly :-(
     */
    
    private static final String ABSOLUTE_PREFIX = "http://miskinhill.com.au";
    private static final Pattern PATTERN = Pattern.compile("((?:href|src)=['\"])" + Pattern.quote(ABSOLUTE_PREFIX) + "([^'\"]*['\"])");
    
    @Override
    protected String postprocessResponse(String responseBody) {
        return PATTERN.matcher(responseBody).replaceAll("$1$2");
    }

}
