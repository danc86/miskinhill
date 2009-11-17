package au.com.miskinhill.web.feeds;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class WorldFeedWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldBeParseable() {
        Model model = ModelFactory.createDefaultModel();
        model.read(BASE.resolve("/feeds/world").toString());
        assertTrue(model.size() >= 25585L);
    }

}
