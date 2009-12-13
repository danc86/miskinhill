package au.com.miskinhill.rdf;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.miskinhill.TestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class HTMLRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    private Model model;
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.load(HTMLRepresentationTest.class, "/au/com/miskinhill/rdf/test.xml");
        representation = representationFactory.getRepresentationByFormat("html");
    }
    
    @Test
    public void testJournal() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Journal.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testIssue() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Issue.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testAuthor() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/authors/test-author"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Author.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testForum() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Forum.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testClass() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/Book"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Class.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testProperty() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/startPage"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Property.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testBook() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/cited/books/test"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Book.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testReview() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Review.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testArticle() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Article.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testObituary() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/Obituary.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testCitedArticle() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/cited/journals/asdf/1:1/article"));
        String expected = TestUtil.exhaust(this.getClass().getResourceAsStream("template/html/CitedArticle.out.xml"));
        assertEquals(expected.trim(), result.trim());
    }

}
