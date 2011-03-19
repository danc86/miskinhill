package au.com.miskinhill.domain;

import static au.com.miskinhill.domain.FieldMatcher.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;

public class AuthorUnitTest {
    
    private Author author;
    
    @Before
    public void setUpTestAuthor() {
        Model model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("author.ttl"), null, "TURTLE");
        
        FulltextFetcher fulltextFetcher = createMock(FulltextFetcher.class);
        replay(fulltextFetcher);
        
        author = new Author(model.getResource("http://miskinhill.com.au/authors/test-author"), fulltextFetcher);
    }
    
    @After
    public void verifyFulltextFetcher() {
        verify(author.fulltextFetcher);
    }
    
    @Test
    public void testAnchorText() {
        assertEquals("Aureliano Buendía", author.getAnchorText());
    }

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
		Document doc = new Document();
		author.addFieldsToDocument("", doc);
		
		assertThat(doc.getFields(), hasItems(
		        indexedUnstoredFieldWithName("http://xmlns.com/foaf/0.1/name"), // XXX assert content?
	            storedIndexedFieldWithNameAndValue("type", "http://miskinhill.com.au/rdfschema/1.0/Author"),
	            storedIndexedFieldWithNameAndValue("url", "http://miskinhill.com.au/authors/test-author")));
	}

}
