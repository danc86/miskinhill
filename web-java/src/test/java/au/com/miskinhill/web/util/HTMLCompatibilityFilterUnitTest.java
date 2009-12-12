package au.com.miskinhill.web.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class HTMLCompatibilityFilterUnitTest {
    
    @Test
    public void shouldExpandSelfClosingScript() {
        String result = new HTMLCompatibilityFilter().postprocessResponse("<script src=\"asdf\" />");
        assertThat(result, equalTo("<script src=\"asdf\"></script>"));
    }
    
    @Test
    public void shouldExpandSelfClosingSpan() {
        String result = new HTMLCompatibilityFilter().postprocessResponse("<span class=\"asdf\" />");
        assertThat(result, equalTo("<span class=\"asdf\"></span>"));
    }
    
    @Test
    public void shouldExpandSelfClosingAbbr() {
        String result = new HTMLCompatibilityFilter().postprocessResponse("<abbr title=\"asdf\" />");
        assertThat(result, equalTo("<abbr title=\"asdf\"></abbr>"));
    }
    
    @Test
    public void shouldExpandSelfClosingA() {
        String result = new HTMLCompatibilityFilter().postprocessResponse("<a href=\"xyz\" />");
        assertThat(result, equalTo("<a href=\"xyz\"></a>"));
    }

}
