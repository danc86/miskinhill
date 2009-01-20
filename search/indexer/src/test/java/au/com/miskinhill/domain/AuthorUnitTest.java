package au.com.miskinhill.domain;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class AuthorUnitTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
		Model model = ModelFactory.createDefaultModel();
		model.read(this.getClass().getResourceAsStream("author.ttl"), null, "TURTLE");
		
		FulltextFetcher fulltextFetcher = createMock(FulltextFetcher.class);
		replay(fulltextFetcher);
		
		Author author = new Author(model.getResource("http://miskinhill.com.au/authors/test-author"), fulltextFetcher);
		
		Document doc = new Document();
		author.addFieldsToDocument("", doc);
		
		assertThat((List<Field>) doc.getFields(), hasItems(
				// Lucene lameness: Field has no sensible equals(), so we have to do customer matchers *sigh*
				new BaseMatcher<Field>() {
					@Override
					public boolean matches(Object field_) {
						Field field = (Field) field_;
						return (field.name().equals("anchor") && 
								field.stringValue().equals("Aureliano Buendía") &&
								field.isStored() && 
								!field.isIndexed());
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("anchor field with value \"Aureliano Buendía\"");
					}
				}, 
				new BaseMatcher<Field>() {
					@Override
					public boolean matches(Object field_) {
						Field field = (Field) field_;
						return (field.name().equals("http://xmlns.com/foaf/0.1/name") &&
								// XXX assert content?
								!field.isStored() &&
								field.isIndexed());
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("foaf:name field");
					}
				}, 
                new BaseMatcher<Field>() {
                    @Override
                    public boolean matches(Object field_) {
                        Field field = (Field) field_;
                        return (field.name().equals("type") &&
                                field.stringValue().equals("http://miskinhill.com.au/rdfschema/1.0/Author") &&
                                field.isStored() &&
                                field.isIndexed());
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("type field");
                    }
                }));
		
		verify(fulltextFetcher);
	}

}
