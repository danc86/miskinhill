package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.config.ClientConfig;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.schema.unapi.Format;
import au.com.miskinhill.schema.unapi.Formats;

public class UnapiWebIntegrationTest extends AbstractWebIntegrationTest {
    
    private static final String ASEES_ID = "http://miskinhill.com.au/journals/asees/";
    private static final String ARTICLE_ID = "http://miskinhill.com.au/journals/asees/22:1-2/lachlan-macquarie-in-russia";

    @Test
    public void testFormats() throws DocumentException {
        String response = Client.create().resource(BASE).path("/unapi")
                .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(doc.getRootElement().getName(), equalTo("formats"));
        for (Iterator<Node> it = doc.getRootElement().nodeIterator(); it.hasNext(); ) {
            assertThat(it.next().getName(), equalTo("format"));
        }
    }
    
    @Test
    public void testFormatsForId() throws DocumentException {
        String response = Client.create().resource(BASE).path("/unapi").queryParam("id", ASEES_ID)
                .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(doc.getRootElement().getName(), equalTo("formats"));
        assertThat(doc.getRootElement().attributeValue("id"), equalTo(ASEES_ID));
        for (Iterator<Node> it = doc.getRootElement().nodeIterator(); it.hasNext(); ) {
            assertThat(it.next().getName(), equalTo("format"));
        }
    }
    
    @Test
    public void testNonexistentId() throws DocumentException {
        try {
            Client.create().resource(BASE).path("/unapi").queryParam("id", "notexist")
                    .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_NOT_FOUND));
        }
    }
    
    @Test
    public void testUnknownFormat() throws DocumentException {
        try {
            Client.create().resource(BASE).path("/unapi").queryParam("id", ASEES_ID).queryParam("format", "notexist")
                    .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE));
        }
    }
    
    @Test
    public void testUnacceptableFormat() throws DocumentException {
        try {
            Client.create().resource(BASE).path("/unapi").queryParam("id", ASEES_ID).queryParam("format", "atom")
                    .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_NOT_ACCEPTABLE));
        }
    }
    
    @Test
    public void testRedirectForIdAndFormat() throws DocumentException {
        try {
            Client client = Client.create();
            client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
            client.resource(BASE).path("/unapi").queryParam("id", ASEES_ID).queryParam("format", "xml")
                    .accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_FOUND));
            assertThat(e.getResponse().getLocation(), equalTo(URI.create(ASEES_ID + ".xml")));
        }
    }
    
    @Test
    public void allListedFormatsShouldWork() throws DocumentException {
        for (String id: new String[] { ASEES_ID, ARTICLE_ID }) {
            Client client = Client.create();
            client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
            Formats formats = client.resource(BASE).path("/unapi").queryParam("id", id).get(Formats.class);
            for (Format format: formats.getFormats()) {
                try {
                    client.resource(BASE).path("/unapi").queryParam("id", id).queryParam("format", format.getName())
                            .get(String.class);
                    fail("should throw");
                } catch (UniformInterfaceException e) {
                    assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_FOUND));
                    assertThat(e.getResponse().getLocation(), equalTo(URI.create(id + "." + format.getName())));
                }
            }
        }
    }

}
