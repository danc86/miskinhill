package au.com.miskinhill.rdf;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class EndnoteRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    @Autowired private Model realModel; // i.e. with real data, not fake test data
    private Representation representation;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("end");
    }
    
    @Test
    public void testMHArticle() throws Exception {
        String result = representation.render(realModel.getResource("http://miskinhill.com.au/journals/asees/22:1-2/lachlan-macquarie-in-russia"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("end/lachlan-macquarie.end"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testCitedArticle() throws Exception {
        String result = representation.render(realModel.getResource("http://miskinhill.com.au/cited/journals/ajph/45:1/all-union-society"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("end/all-union-society.end"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }

}
