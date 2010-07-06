package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.schema.unapi.Format;
import au.com.miskinhill.schema.unapi.Formats;

public class UnapiWebIntegrationTest extends AbstractWebIntegrationTest {
    
    private static final String ASEES_ID = "http://miskinhill.com.au/journals/asees/";
    private static final String ASEES_ID_PARAM = "id=" + ProperURLCodec.encodeUrl(ASEES_ID);
    private static final String ARTICLE_ID = "http://miskinhill.com.au/journals/asees/22:1-2/lachlan-macquarie-in-russia";
    
    @Test
    public void shouldServeAsApplicationXml() {
        ResponseEntity<Formats> response= restTemplate.getForEntity(BASE.resolve("/unapi"), Formats.class);
        assertTrue(response.getHeaders().getContentType().equals(MediaType.APPLICATION_XML));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFormats() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/unapi"), Document.class);
        assertThat(doc.getRootElement().getName(), equalTo("formats"));
        for (Iterator<Node> it = doc.getRootElement().nodeIterator(); it.hasNext(); ) {
            assertThat(it.next().getName(), equalTo("format"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFormatsForId() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/unapi?" + ASEES_ID_PARAM), Document.class);
        assertThat(doc.getRootElement().getName(), equalTo("formats"));
        assertThat(doc.getRootElement().attributeValue("id"), equalTo(ASEES_ID));
        for (Iterator<Node> it = doc.getRootElement().nodeIterator(); it.hasNext(); ) {
            assertThat(it.next().getName(), equalTo("format"));
        }
    }
    
    @Test
    public void testNonexistentId() throws DocumentException {
        assertHttpError(BASE.resolve("/unapi?id=notexist"), HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void testUnknownFormat() throws DocumentException {
        assertHttpError(BASE.resolve("/unapi?" + ASEES_ID_PARAM + "&format=notexist"), HttpStatus.NOT_ACCEPTABLE);
    }
    
    @Test
    public void testUnacceptableFormat() throws DocumentException {
        assertHttpError(BASE.resolve("/unapi?" + ASEES_ID_PARAM + "&format=atom"), HttpStatus.NOT_ACCEPTABLE);
    }
    
    @Test
    public void testRedirectForIdAndFormat() throws DocumentException {
        assertRedirect(BASE.resolve("/unapi?" + ASEES_ID_PARAM + "&format=xml"), URI.create(ASEES_ID + ".xml"));
    }
    
    @Test
    public void allListedFormatsShouldWork() throws DocumentException {
        for (String id: new String[] { ASEES_ID, ARTICLE_ID }) {
            String idParam = "id=" + ProperURLCodec.encodeUrl(id);
            Formats formats = restTemplate.getForObject(BASE.resolve("/unapi?" + idParam), Formats.class);
            for (Format format: formats.getFormats()) {
                assertRedirect(BASE.resolve("/unapi?" + idParam + "&format=" + format.getName()), URI.create(id + "." + format.getName()));
            }
        }
    }

}
