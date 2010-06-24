package au.com.miskinhill.web.feeds;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.dom4j.DocumentException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class WorldFeedWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldBeParseable() {
        Model model = ModelFactory.createDefaultModel();
        model.read(BASE.resolve("/feeds/world").toString());
        assertTrue(model.size() >= 12672L);
    }
    
    @Test
    public void shouldSupportIfModifiedSince() throws DocumentException {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(BASE.resolve("/feeds/world"), byte[].class);
        DateTime lastModified = new DateTime(response.getHeaders().getLastModified());
        assertNotModifiedSince(BASE.resolve("/feeds/world"), lastModified.plusMinutes(1));
    }

}
