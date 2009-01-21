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
		
		assertThat((List<Field>) doc.getFields(), hasItems(
				// Lucene lameness: Field has no sensible equals(), so we have to do customer matchers *sigh*
				new BaseMatcher<Field>() {
					@Override
					public boolean matches(Object field_) {
						Field field = (Field) field_;
						return (field.name().equals("some-prefix http://purl.org/dc/terms/title") &&
								// XXX assert content?
								!field.isStored() &&
								field.isIndexed());
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("dc:title field");
					}
				}, 
                new BaseMatcher<Field>() {
                    @Override
                    public boolean matches(Object field_) {
                        Field field = (Field) field_;
                        return (field.name().equals("some-prefix http://purl.org/dc/terms/creator") &&
                                // XXX assert content?
                                !field.isStored() &&
                                field.isIndexed());
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("dc:creator field");
                    }
                }, 
                new BaseMatcher<Field>() {
                    @Override
                    public boolean matches(Object field_) {
                        Field field = (Field) field_;
                        return (field.name().equals("some-prefix type") &&
                                field.stringValue().equals("http://miskinhill.com.au/rdfschema/1.0/Book") &&
                                field.isStored() &&
                                field.isIndexed());
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("type field");
                    }
                }, 
                new BaseMatcher<Field>() {
                    @Override
                    public boolean matches(Object field_) {
                        Field field = (Field) field_;
                        return (field.name().equals("some-prefix url") &&
                                field.stringValue().equals("http://miskinhill.com.au/test-books/test-book") &&
                                field.isStored() &&
                                field.isIndexed());
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("url field");
                    }
                }));
		
		verify(fulltextFetcher);
	}

}
