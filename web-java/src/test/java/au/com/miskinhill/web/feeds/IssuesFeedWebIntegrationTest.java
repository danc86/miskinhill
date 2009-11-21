package au.com.miskinhill.web.feeds;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.dom4j.XPath;

import com.sun.jersey.api.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class IssuesFeedWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void titleShouldBeThere() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/issues")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(xpath("/atom:feed/atom:title[@type='text']").selectSingleNode(doc).getText(),
                equalTo("Miskin Hill Journal Issues"));
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("atom", "http://www.w3.org/2005/Atom"));
        return xpath;
    }

}
