package au.com.miskinhill.web.rdf;

import static au.com.miskinhill.MiskinHillMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import org.dom4j.Document;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

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
        checkContentTypeForPathAndAccept("/journals/asees/", "application/foobar, text/vcard", MediaType.TEXT_HTML);
    }
    
    @Test
    public void shouldReturnDefaultTypeIfKnownButInapplicableTypesAreRequested() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/atom+xml, application/x-endnote-refer", MediaType.TEXT_HTML);
    }
    
    @Test
    public void shouldReturnHtmlAsDefaultType() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", null, MediaType.TEXT_HTML);
    }
    
    @Test
    public void shouldReturnHtmlForWildcardAccept() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "*/*", MediaType.TEXT_HTML);
    }
    
    @Test
    public void should_return_html_for_application_xhtml_xml() throws Exception {
        checkContentTypeForPathAndAccept("/journals/asees/", "application/xhtml+xml, application/mods+xml", MediaType.TEXT_HTML);
    }
    
    @Test
    public void shouldGive404ForInapplicableFormatInExtension() throws Exception {
        assertHttpError(BASE.resolve("/journals/asees/.end"), HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        assertRedirect(BASE.resolve("/journals/asees"), URI.create("http://miskinhill.com.au/journals/asees/"));
    }
    
    @SuppressWarnings("unchecked") // joda
    @Test
    public void shouldAddLastModifiedHeader() throws Exception {
        ResponseEntity<Document> response = restTemplate.getForEntity(BASE.resolve("/journals/asees/22:1-2/"), Document.class);
        assertThat(new DateTime(response.getHeaders().getLastModified()), greaterThan(new DateTime("2010-06-20T15:00:00+10:00")));
    }
    
    @Test
    public void shouldSupportHEADMethod() throws Exception {
        HttpHeaders headers = restTemplate.headForHeaders(BASE.resolve("/journals/asees/22:1-2/"));
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.TEXT_HTML));
        assertThat(headers.getLastModified(), greaterThan(0L));
    }
    
    private void checkContentTypeForPathAndAccept(String pathInfo, final String accept, final MediaType expectedContentType) throws Exception {
        restTemplate.execute(BASE.resolve(pathInfo), HttpMethod.GET, new RequestCallback() {
            @Override
            public void doWithRequest(ClientHttpRequest request) throws IOException {
                request.getHeaders().set("Accept", accept);
            }
        }, new ResponseExtractor<Object>() {
            @Override
            public Object extractData(ClientHttpResponse response) throws IOException {
                assertTrue(expectedContentType.isCompatibleWith(response.getHeaders().getContentType())); // don't care about content-type params
                return null;
            }
        });
    }
    
}
