package au.com.miskinhill.domain;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ArticleUnitTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
		Model model = ModelFactory.createDefaultModel();
		model.read(this.getClass().getResourceAsStream("article.ttl"), null, "TURTLE");
		
		FulltextFetcher fulltextFetcher = createMock(FulltextFetcher.class);
		expect(fulltextFetcher.fetch(isA(String.class))).andReturn(new ByteArrayInputStream(
				("<div xmlns=\"http://www.w3.org/1999/xhtml\" class=\"body-text\" lang=\"en\">\n" +
				"<h3>1. Introduction</h3>\n" +
				"<p>An important and complex area of stress in Russian, which has to date received\n" + 
				"insufficient attention, is variation in stress. By variation in stress is meant\n" + 
				"the possibility of two (or more, in theory, but rarely in practice) syllables\n" + 
				"on which the stress may fall in a given word form. Thus, for example, the\n" +  
				"plural short form of the adjective <em>ве́рный</em> ‘faithful’ is given in").getBytes("UTF-8")));
		replay(fulltextFetcher);
		
		Article article = new Article(model.getResource("http://miskinhill.com.au/journals/test/1:1/test-article"), fulltextFetcher);
		
		Document doc = new Document();
		article.addFieldsToDocument("", doc);
		
		assertThat((List<Field>) doc.getFields(), hasItems(
				new BaseMatcher<Field>() {
					@Override
					public boolean matches(Object field_) {
						Field field = (Field) field_;
						return (field.name().equals("anchor") && 
								field.stringValue().equals("One hundred years of solitude") &&
								field.isStored() && 
								!field.isIndexed());
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("anchor field with value \"One hundred years of solitude\"");
					}
				}, 
				new BaseMatcher<Field>() {
					@Override
					public boolean matches(Object field_) {
						Field field = (Field) field_;
						return (field.name().equals("content") &&
								// XXX assert content?
								!field.isStored() &&
								field.isIndexed());
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("content field");
					}
				}));
		
		verify(fulltextFetcher);
	}

}
