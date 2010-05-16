package au.com.miskinhill.domain;

import static au.com.miskinhill.domain.FieldMatcher.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;

public class ReviewUnitTest {
    
    private IMocksControl mockControl;
    private FulltextFetcher fulltextFetcher;
    private Model model;
    
    @Before
    public void setUp() throws Exception {
        mockControl = createControl();
        fulltextFetcher = mockControl.createMock(FulltextFetcher.class);
        model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("review.ttl"), null, "TURTLE");
    }
    
    private void expectFulltext() throws Exception {
        expect(fulltextFetcher.fetchFulltext(isA(String.class)))
                .andReturn(new ByteArrayInputStream(
                    ("<div xmlns=\"http://www.w3.org/1999/xhtml\" class=\"body-text\" lang=\"en\">\n" +
                    "<p>An important and complex area of stress in Russian, which has to date received\n" + 
                    "insufficient attention, is variation in stress. By variation in stress is meant\n" + 
                    "the possibility of two (or more, in theory, but rarely in practice) syllables\n" + 
                    "on which the stress may fall in a given word form. Thus, for example, the\n" +  
                    "plural short form of the adjective <em>ве́рный</em> ‘faithful’ is given in").getBytes("UTF-8")));
    }
    
    @Test
    public void testAnchorText() throws Exception {
        mockControl.replay();
        Review review = new Review(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/test-review"), fulltextFetcher);
        assertThat(review.getAnchorText(), equalTo(
                "Dieter Aaron, <em>Writers on the left: episodes in American literary communism</em> (1961); " +
                "Robert Service, <em>Stalin: a biography</em> (2004)"));
        mockControl.verify();
    }

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
	    expectFulltext();
	    mockControl.replay();
	    Review review = new Review(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/test-review"), fulltextFetcher);
		Document doc = new Document();
		review.addFieldsToDocument("", doc);
		mockControl.verify();
		
		assertThat((List<Field>) doc.getFields(), hasItems(
		        indexedUnstoredFieldWithName("content"),
		        storedIndexedFieldWithNameAndValue("type", "http://miskinhill.com.au/rdfschema/1.0/Review"),
		        storedIndexedFieldWithNameAndValue("url", "http://miskinhill.com.au/journals/test/1:1/reviews/test-review"),
                indexedUnstoredFieldWithName("http://miskinhill.com.au/rdfschema/1.0/reviews http://purl.org/dc/terms/title"),
                indexedUnstoredFieldWithName("http://miskinhill.com.au/rdfschema/1.0/reviews http://purl.org/dc/terms/creator http://xmlns.com/foaf/0.1/name"),
                indexedUnstoredFieldWithName("http://miskinhill.com.au/rdfschema/1.0/reviews http://purl.org/dc/terms/date")));
	}

}
