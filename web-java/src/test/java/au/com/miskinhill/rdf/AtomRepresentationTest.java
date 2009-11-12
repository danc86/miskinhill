package au.com.miskinhill.rdf;

import static org.hamcrest.CoreMatchers.*;

import java.util.Collections;

import org.dom4j.XPath;

import org.dom4j.DocumentException;

import org.dom4j.DocumentHelper;

import org.dom4j.Document;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class AtomRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    private Model model;
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.load(AtomRepresentationTest.class, "/au/com/miskinhill/rdf/test.xml");
        representation = representationFactory.getRepresentationByFormat("atom");
    }
    
    @Test
    public void shouldHaveFeedUpdatedTimestamp() throws Exception {
        String updated = xpath("/atom:feed/atom:updated").selectSingleNode(render()).getText();
        assertThat(updated, equalTo("2009-06-15T18:21:32+10:00"));
    }
    
    @Test
    public void shouldHaveEntryTimestamps() throws Exception {
        String published = xpath("/atom:feed/atom:entry/atom:published").selectSingleNode(render()).getText();
        String updated = xpath("/atom:feed/atom:entry/atom:updated").selectSingleNode(render()).getText();
        assertThat(published, equalTo("2009-06-15T18:21:32+10:00"));
        assertThat(updated, equalTo("2009-06-15T18:21:32+10:00"));
    }
    
    private Document render() throws DocumentException {
        String result = representation.render(model.getResource("http://miskinhill.com.au/"));
        return DocumentHelper.parseText(result);
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("atom", "http://www.w3.org/2005/Atom"));
        return xpath;
    }

}
