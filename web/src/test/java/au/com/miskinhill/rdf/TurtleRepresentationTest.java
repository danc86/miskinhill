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
@ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context-with-fake-model.xml")
public class TurtleRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    @Autowired private Model model;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("ttl");
    }
    
    @Test
    public void testJournal() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/turtle/Journal.out.txt"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testObituary() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/turtle/Obituary.out.txt"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }

}
