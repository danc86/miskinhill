package au.com.miskinhill.domain;

import static au.com.miskinhill.domain.FieldMatcher.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.lucene.document.Document;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;

public class ArticleUnitTest {

    private IMocksControl mockControl;
    private FulltextFetcher fulltextFetcher;
    
    @Before
    public void setUp() throws Exception {
        mockControl = createControl();
        fulltextFetcher = mockControl.createMock(FulltextFetcher.class);
    }

    private Model model() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("article.ttl"), null, "TURTLE");
        return model;
    }

    private Model citedArticleModel() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("cited-article.ttl"), null, "TURTLE");
        return model;
    }
    
    private void expectFullText() throws Exception {
        expect(fulltextFetcher.fetchFulltext(isA(String.class)))
                .andReturn(new ByteArrayInputStream(
                    ("<div xmlns=\"http://www.w3.org/1999/xhtml\" class=\"body-text\" lang=\"en\">\n" +
                    "<h3>1. Introduction</h3>\n" +
                    "<p>An important and complex area of stress in Russian, which has to date received\n" + 
                    "insufficient attention, is variation in stress. By variation in stress is meant\n" + 
                    "the possibility of two (or more, in theory, but rarely in practice) syllables\n" + 
                    "on which the stress may fall in a given word form. Thus, for example, the\n" +  
                    "plural short form of the adjective <em>ве́рный</em> ‘faithful’ is given in").getBytes("UTF-8")));
    }
    
    @Test
    public void testAnchorText() throws Exception {
        mockControl.replay();
        Article article = new Article(model().getResource("http://miskinhill.com.au/journals/test/1:1/test-article"),
                fulltextFetcher);
        assertEquals("One hundred years of solitude", article.getAnchorText());
        mockControl.verify();
    }

	@SuppressWarnings("unchecked")
	@Test
	public void testAddFieldsToDocument() throws Exception {
	    expectFullText();
        mockControl.replay();
        Article article = new Article(model().getResource("http://miskinhill.com.au/journals/test/1:1/test-article"), fulltextFetcher);
		Document doc = new Document();
		article.addFieldsToDocument("", doc);
		mockControl.verify();
		
		assertThat(doc.getFields(), hasItems(
		        indexedUnstoredFieldWithName("content"), // XXX assert content?
		        storedIndexedFieldWithNameAndValue("type", "http://miskinhill.com.au/rdfschema/1.0/Article"),
		        storedIndexedFieldWithNameAndValue("url", "http://miskinhill.com.au/journals/test/1:1/test-article")));
	}

    @Test
    public void mhArticlesShouldBeTopLevel() throws Exception {
        mockControl.replay();
        Article article = new Article(model().getResource("http://miskinhill.com.au/journals/test/1:1/test-article"),
                fulltextFetcher);
        mockControl.verify();
        assertThat(article.isTopLevel(), equalTo(true));
    }

    @Test
    public void citedArticlesShouldNotBeTopLevel() throws Exception {
        mockControl.replay();
        Article article = new Article(citedArticleModel().getResource(
                "http://miskinhill.com.au/cited/journals/test/1:1/test-cited-article"), fulltextFetcher);
        mockControl.verify();
        assertThat(article.isTopLevel(), equalTo(false));
    }

}
