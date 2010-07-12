package au.com.miskinhill.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context-with-fake-model.xml")
public class OAIDCRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    @Autowired private Model model;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("oai_dc");
    }
    
    @Test
    public void shouldIncludeTitleWithoutMarkup() throws Exception {
        Document doc = renderArticle();
        assertThat(xpath("/oai_dc:dc/dc:title").selectSingleNode(doc).getText(), equalTo("Moscow 1937: the interpreter’s story"));
    }
    
    @Test
    public void shouldIncludeAuthorsWithSurnameFirst() throws Exception {
        Document doc = renderArticle();
        assertThat(xpath("/oai_dc:dc/dc:creator").selectSingleNode(doc).getText(), equalTo("Author, Test"));
    }
    
    @Test
    public void shouldIncludeIssuePublicationDateAsDate() throws Exception {
        Document doc = renderArticle();
        assertThat(xpath("/oai_dc:dc/dc:date").selectSingleNode(doc).getText(), equalTo("2008-02-01"));
    }
    
    @Test
    public void shouldIncludeJournalPublisher() throws Exception {
        Document doc = renderArticle();
        assertThat(xpath("/oai_dc:dc/dc:publisher").selectSingleNode(doc).getText(), equalTo("Awesome Publishing House"));
    }
    
    @Test
    public void shouldIncludeCleanedUpLCSHAsSubject() throws Exception {
        Document doc = renderArticle();
        assertThat(xpath("/oai_dc:dc/dc:subject").selectSingleNode(doc).getText(), equalTo("Moscow (Russia) – History"));
    }
    
    private Document renderArticle() throws DocumentException {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        return DocumentHelper.parseText(result);
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentHelper.createXPath(expression);
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
        xpath.setNamespaceURIs(namespaces);
        return xpath;
    }

}
