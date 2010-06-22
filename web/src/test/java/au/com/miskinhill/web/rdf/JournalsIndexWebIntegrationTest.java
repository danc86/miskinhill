package au.com.miskinhill.web.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class JournalsIndexWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        assertRedirect(BASE.resolve("/journals"), BASE.resolve("/journals/"), HttpStatus.MOVED_PERMANENTLY);
    }
    
    @Test
    public void shouldListASEES() throws Exception {
        Document doc = restTemplate.getForObject(BASE.resolve("/journals/"), Document.class);
        assertThat(xpath("//html:ul[@class='journals']/html:li[1]").selectSingleNode(doc).getStringValue(),
                    equalTo("Australian Slavonic and East European Studies"));
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }

}
