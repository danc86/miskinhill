package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class AttributeRenderersUnitTest {
    
    private final AttributeRenderers renderers = new AttributeRenderers();
    
    @Test
    public void shouldEscapeXml() {
        assertThat(renderers.escapeXml("<>&\"á asdf"), equalTo("&lt;&gt;&amp;&quot;&#225; asdf"));
    }

}
