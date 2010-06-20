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

public class RDFSchemaWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        assertRedirect(BASE.resolve("/rdfschema/1.0"), BASE.resolve("/rdfschema/1.0/"), HttpStatus.TEMPORARY_REDIRECT);
    }
    
    @Test
    public void shouldListTranslatorAsProperty() throws Exception {
        Document doc = restTemplate.getForObject(BASE.resolve("/rdfschema/1.0/"), Document.class);
        assertThat(xpath("//html:h3[text()='Properties']/following-sibling::html:ul/html:li/html:a[@href='/rdfschema/1.0/translator']")
                .selectSingleNode(doc).getStringValue(),
                equalTo("translator"));
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }
    
}
