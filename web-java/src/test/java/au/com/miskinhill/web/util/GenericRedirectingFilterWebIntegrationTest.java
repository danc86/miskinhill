package au.com.miskinhill.web.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import au.com.miskinhill.AbstractJWebUnitTest;

public class GenericRedirectingFilterWebIntegrationTest extends AbstractJWebUnitTest {
    
    @Test
    public void shouldRedirectOldCitationUrls() throws Exception {
        tester.beginAt("/journals/asees/21:1-2/writing-manuscript-pasternaks-povest/citations/abc123");
        assertThat(tester.getTestingEngine().getPageURL(),
                equalTo(BASE.resolve("/journals/asees/21:1-2/writing-manuscript-pasternaks-povest").toURL()));
    }
    
    @Test
    public void shouldRedirectOldCitationUrlsWithFormatExtension() throws Exception {
        tester.beginAt("/journals/asees/21:1-2/writing-manuscript-pasternaks-povest/citations/abc123.xml");
        assertThat(tester.getTestingEngine().getPageURL(),
                equalTo(BASE.resolve("/journals/asees/21:1-2/writing-manuscript-pasternaks-povest.xml").toURL()));
    }

}
