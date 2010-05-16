package au.com.miskinhill.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class MARCXMLRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    private Model model;
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.load(MARCXMLRepresentationTest.class, "/au/com/miskinhill/rdf/test.xml");
        representation = representationFactory.getRepresentationByFormat("marcxml");
    }
    
    @Test
    public void shouldPutBeginningDateIn008() throws Exception {
        String f008 = xpath("marcxml:collection/marcxml:record/marcxml:controlfield[@tag='008']").selectSingleNode(render()).getText();
        assertThat(f008.substring(7, 11), equalTo("1987"));
    }
    
    @Test
    public void shouldPutIssnIn022() throws Exception {
        String f022a = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='022']/marcxml:subfield[@code='a']").selectSingleNode(render()).getText();
        assertThat(f022a, equalTo("12345678"));
    }
    
    @Test
    public void shouldPutMHIn040() throws Exception {
        // cataloguing source
        String f040a = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='040']/marcxml:subfield[@code='a']").selectSingleNode(render()).getText();
        assertThat(f040a, equalTo("Miskin Hill Academic Publishing"));
    }
    
    @Test
    public void shouldPutLanguageCodesIn041() throws Exception {
        List<Node> nodes = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='041']/marcxml:subfield[@code='a']").selectNodes(render());
        assertThat(nodes.size(), equalTo(2));
        assertThat(nodes.get(0).getText(), equalTo("eng"));
        assertThat(nodes.get(1).getText(), equalTo("rus"));
    }
    
    @Test
    public void shouldPutTitleIn245() throws Exception {
        String f245a = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='245']/marcxml:subfield[@code='a']").selectSingleNode(render()).getText();
        assertThat(f245a, equalTo("Test Journal of Good Stuff"));
    }
    
    @Test
    public void shouldPutPublisherDetailsIn260() throws Exception {
        String f260a = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='260']/marcxml:subfield[@code='a']").selectSingleNode(render()).getText();
        assertThat(f260a, equalTo("St Lucia, Qld. :"));
        String f260b = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='260']/marcxml:subfield[@code='b']").selectSingleNode(render()).getText();
        assertThat(f260b, equalTo("Awesome Publishing House,"));
        String f260c = xpath("marcxml:collection/marcxml:record/marcxml:datafield[@tag='260']/marcxml:subfield[@code='c']").selectSingleNode(render()).getText();
        assertThat(f260c, equalTo("1987-"));
    }
    
    private Document render() throws DocumentException {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        return DocumentHelper.parseText(result);
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("marcxml", "http://www.loc.gov/MARC21/slim"));
        return xpath;
    }

}
