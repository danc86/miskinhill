package au.com.miskinhill.web.rdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context.xml")
public class RDFRequestHandlerIntegrationTest {
    
    @Autowired private RDFRequestHandler handler;
    
    @Test
    public void shouldAsdf() throws Exception {
        MockHttpServletRequest request = request("/journals/asees/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handleRequest(request, response);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
//        assertThat(response.getContentAsString(), equalTo("asdf"));
    }
    
    @Test
    public void shouldSupportExtensionStyleUrls() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/.mods", null, "application/mods+xml");
    }
    
    @Test
    public void shouldDoContentNegotiation() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "text/html; q=0.5, application/mods+xml", "application/mods+xml");
    }
    
    @Test
    public void shouldReturnDefaultTypeIfNoKnownTypesAreRequested() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/foobar, text/vcard", "text/html");
    }
    
    @Test
    public void shouldReturnDefaultTypeIfKnownButInapplicableTypesAreRequested() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/atom+xml, application/x-endnote-refer", "text/html");
    }
    
    @Test
    public void shouldReturnHtmlAsDefaultType() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", null, "text/html");
    }
    
    @Test
    public void shouldReturnHtmlForWildcardAccept() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "*/*", "text/html");
    }
    
    @Test
    public void shouldGive404ForInapplicableFormatInExtension() throws Exception {
        MockHttpServletRequest request = request("/journals/asees/.end");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handleRequest(request, response);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_NOT_FOUND));
    }
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        MockHttpServletRequest request = request("/journals/asees");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handleRequest(request, response);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_FOUND));
        assertThat((String) response.getHeader("Location"), equalTo("http://miskinhill.com.au/journals/asees/"));
    }
    
    private void checkContentTypeForPathAndAccept(String pathInfo, String accept, String expectedContentType) throws Exception {
        MockHttpServletRequest request = request(pathInfo);
        if (accept != null) request.addHeader("Accept", accept);
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handleRequest(request, response);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
        assertThat(response.getContentType(), equalTo(expectedContentType));
    }
    
    private MockHttpServletRequest request(String pathInfo) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setPathInfo(pathInfo);
        return request;
    }

}
