package au.com.miskinhill.rdf;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class BibtexRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    @Autowired @Qualifier("model") private Model realModel; // i.e. with real data, not fake test data
    private Representation representation;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("bib");
    }
    
    @Test
    public void testMHArticle() throws Exception {
        String result = representation.render(realModel.getResource("http://miskinhill.com.au/journals/asees/22:1-2/lachlan-macquarie-in-russia"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("bib/lachlan-macquarie.bib"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testCitedArticle() throws Exception {
        String result = representation.render(realModel.getResource("http://miskinhill.com.au/cited/journals/ajph/45:1/all-union-society"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("bib/all-union-society.bib"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testId() {
        String id = ((BibtexRepresentation) representation).id(
                realModel.createResource("http://miskinhill.com.au/cited/journals/minuvshee/14/voks"));
        assertEquals("Голубев1993", id);
    }
    
    @Test
    public void testIdForAuthorlessArticle() {
        String id = ((BibtexRepresentation) representation).id(
                realModel.createResource("http://miskinhill.com.au/cited/journals/metally-evrazii/1997:1/do-srednego-timana"));
        assertEquals("ДосредногоТиманарукойподат", id);
    }

}
