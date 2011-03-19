package au.com.miskinhill.domain;

import static au.com.miskinhill.domain.FieldMatcher.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.lucene.document.Document;
import org.junit.Test;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;

public class BookUnitTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
		Model model = ModelFactory.createDefaultModel();
		model.read(this.getClass().getResourceAsStream("book.ttl"), null, "TURTLE");
		
		FulltextFetcher fulltextFetcher = createMock(FulltextFetcher.class);
		replay(fulltextFetcher);
		
		Book book = new Book(model.getResource("http://miskinhill.com.au/test-books/test-book"), fulltextFetcher);
		
		Document doc = new Document();
		book.addFieldsToDocument("some-prefix ", doc);
		
		assertThat(doc.getFields(), hasItems(
		        indexedUnstoredFieldWithName("some-prefix http://purl.org/dc/terms/title"), // XXX assert content?
		        indexedUnstoredFieldWithName("some-prefix http://purl.org/dc/terms/creator http://xmlns.com/foaf/0.1/name"), // XXX assert content?
                storedIndexedFieldWithNameAndValue("some-prefix type", "http://miskinhill.com.au/rdfschema/1.0/Book"),
                storedIndexedFieldWithNameAndValue("some-prefix url", "http://miskinhill.com.au/test-books/test-book")));
		
		verify(fulltextFetcher);
	}

}
