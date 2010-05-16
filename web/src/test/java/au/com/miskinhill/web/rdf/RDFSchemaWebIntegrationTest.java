package au.com.miskinhill.web.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class RDFSchemaWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        try {
            Client client = Client.create();
            client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
            client.resource(BASE).path("/rdfschema/1.0").get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_TEMPORARY_REDIRECT));
            assertThat(e.getResponse().getLocation(), equalTo(BASE.resolve("/rdfschema/1.0/")));
        }
    }
    
    @Test
    public void shouldListTranslatorAsProperty() throws Exception {
        String response = Client.create().resource(BASE).path("/rdfschema/1.0/")
                .accept(MediaType.TEXT_HTML_TYPE).get(String.class);
        Document doc = parse(response);
        assertThat(xpath("//html:h3[text()='Properties']/following-sibling::html:ul/html:li/html:a[@href='/rdfschema/1.0/translator']")
                .selectSingleNode(doc).getStringValue(),
                equalTo("translator"));
    }
    
    private Document parse(String response) throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new XhtmlEntityResolver());
        return reader.read(new StringReader(response));
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }
    
}
