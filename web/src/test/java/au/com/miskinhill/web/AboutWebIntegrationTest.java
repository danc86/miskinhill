package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class AboutWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        assertRedirect(BASE.resolve("/about"), BASE.resolve("/about/"));
    }
    
    @Test
    public void shouldHaveCorrectTitle() throws Exception {
        Document doc = restTemplate.getForObject(BASE.resolve("/about/"), Document.class);
        assertThat(xpath("//html:head/html:title").selectSingleNode(doc).getStringValue(),
                equalTo("About - Miskin Hill"));
    }
    
    @Test
    public void shouldBeDecorated() throws Exception {
        Document doc = restTemplate.getForObject(BASE.resolve("/about/"), Document.class);
        assertThat(xpath("//html:head/html:link[@rel='stylesheet' and @type='text/css' and @href='/style/common.css']").selectSingleNode(doc),
                not(nullValue()));
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }

}
