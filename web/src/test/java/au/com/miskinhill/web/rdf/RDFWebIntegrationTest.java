package au.com.miskinhill.web.rdf;

import static au.com.miskinhill.MiskinHillMatchers.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import org.joda.time.DateTime;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class RDFWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldSupportExtensionStyleUrls() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/.mods", null, new MediaType("application", "mods+xml"));
    }
    
    @Test
    public void shouldDoContentNegotiation() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "text/html; q=0.5, application/mods+xml", new MediaType("application", "mods+xml"));
    }
    
    @Test
    public void shouldReturnDefaultTypeIfNoKnownTypesAreRequested() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/foobar, text/vcard", MediaType.TEXT_HTML_TYPE);
    }
    
    @Test
    public void shouldReturnDefaultTypeIfKnownButInapplicableTypesAreRequested() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/atom+xml, application/x-endnote-refer", MediaType.TEXT_HTML_TYPE);
    }
    
    @Test
    public void shouldReturnHtmlAsDefaultType() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", null, MediaType.TEXT_HTML_TYPE);
    }
    
    @Test
    public void shouldReturnHtmlForWildcardAccept() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "*/*", MediaType.TEXT_HTML_TYPE);
    }
    
    @Test
    public void shouldGive404ForInapplicableFormatInExtension() throws Exception {
        try {
            Client.create().resource(BASE).path("/journals/asees/.end").get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_NOT_FOUND));
        }
    }
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        try {
            Client client = Client.create();
            client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
            client.resource(BASE).path("/journals/asees").get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_FOUND));
            assertThat(e.getResponse().getLocation(), equalTo(URI.create("http://miskinhill.com.au/journals/asees/")));
        }
    }
    
    @SuppressWarnings("unchecked") // joda
    @Test
    public void shouldAddLastModifiedHeader() throws Exception {
        ClientResponse response = Client.create().resource(BASE).path("/journals/asees/22:1-2/").get(ClientResponse.class);
        assertThat(new DateTime(response.getLastModified()), greaterThan(new DateTime("2010-06-20T15:00:00+10:00")));
    }
    
    private void checkContentTypeForPathAndAccept(String pathInfo, String accept, MediaType expectedContentType) throws Exception {
        ClientResponse response = Client.create().resource(BASE).path(pathInfo)
                .header("Accept", accept).get(ClientResponse.class);
        assertTrue(expectedContentType.isCompatible(response.getType())); // don't care about content-type params
    }
    
}
