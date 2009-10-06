package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.InputStream;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.miskinhill.rdftemplate.datatype.DateDataType;

public class SelectorEvaluationUnitTest {
    
    private Model m;
    private Resource journal, issue, article, citedArticle, author, anotherAuthor, book, review, anotherReview, obituary, en, ru;
    private SelectorFactory selectorFactory;
    
    @BeforeClass
    public static void ensureDatatypesRegistered() {
        DateDataType.registerStaticInstance();
    }
    
    @Before
    public void setUp() {
        m = ModelFactory.createDefaultModel();
        InputStream stream = this.getClass().getResourceAsStream("/au/com/miskinhill/rdftemplate/test-data.xml");
        m.read(stream, "");
        journal = m.createResource("http://miskinhill.com.au/journals/test/");
        issue = m.createResource("http://miskinhill.com.au/journals/test/1:1/");
        article = m.createResource("http://miskinhill.com.au/journals/test/1:1/article");
        citedArticle = m.createResource("http://miskinhill.com.au/cited/journals/asdf/1:1/article");
        author = m.createResource("http://miskinhill.com.au/authors/test-author");
        anotherAuthor = m.createResource("http://miskinhill.com.au/authors/another-author");
        book = m.createResource("http://miskinhill.com.au/cited/books/test");
        review = m.createResource("http://miskinhill.com.au/journals/test/1:1/reviews/review");
        anotherReview = m.createResource("http://miskinhill.com.au/journals/test/2:1/reviews/another-review");
        obituary = m.createResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe");
        en = m.createResource("http://www.lingvoj.org/lang/en");
        ru = m.createResource("http://www.lingvoj.org/lang/ru");
        selectorFactory = new AntlrSelectorFactory();
    }
    
    @Test
    public void shouldEvaluateTraversal() {
        RDFNode result = selectorFactory.get("dc:creator").withResultType(RDFNode.class).singleResult(article);
        assertThat(result, equalTo((RDFNode) author));
    }
    
    @Test
    public void shouldEvaluateMultipleTraversals() throws Exception {
        RDFNode result = selectorFactory.get("dc:creator/foaf:name")
                .withResultType(RDFNode.class).singleResult(article);
        assertThat(((Literal) result).getString(), equalTo("Test Author"));
    }
    
    @Test
    public void shouldEvaluateInverseTraversal() throws Exception {
        List<RDFNode> results = selectorFactory.get("!mhs:isIssueOf/!dc:isPartOf")
                .withResultType(RDFNode.class).result(journal);
        assertThat(results.size(), equalTo(4));
        assertThat(results, hasItems((RDFNode) article, (RDFNode) review, (RDFNode) anotherReview, (RDFNode) obituary));
    }
    
    @Test
    public void shouldEvaluateSortOrder() throws Exception {
        List<RDFNode> results = selectorFactory.get("dc:language(lingvoj:iso1#comparable-lv)")
                .withResultType(RDFNode.class).result(journal);
        assertThat(results.size(), equalTo(2));
        assertThat(results.get(0), equalTo((RDFNode) en));
        assertThat(results.get(1), equalTo((RDFNode) ru));
    }
    
    @Test
    public void shouldEvaluateReverseSortOrder() throws Exception {
        List<RDFNode> results = selectorFactory.get("dc:language(~lingvoj:iso1#comparable-lv)")
                .withResultType(RDFNode.class).result(journal);
        assertThat(results.size(), equalTo(2));
        assertThat(results.get(0), equalTo((RDFNode) ru));
        assertThat(results.get(1), equalTo((RDFNode) en));
    }
    
    @Test
    public void shouldEvaluateComplexSortOrder() throws Exception {
        List<RDFNode> results = selectorFactory.get("!mhs:reviews(dc:isPartOf/mhs:publicationDate#comparable-lv)")
                .withResultType(RDFNode.class).result(book);
        assertThat(results.size(), equalTo(2));
        assertThat(results.get(0), equalTo((RDFNode) review));
        assertThat(results.get(1), equalTo((RDFNode) anotherReview));
    }
    
    @Test
    public void shouldEvaluateUriAdaptation() throws Exception {
        String result = selectorFactory.get("mhs:coverThumbnail#uri")
                .withResultType(String.class).singleResult(issue);
        assertThat(result, equalTo("http://miskinhill.com.au/journals/test/1:1/cover.thumb.jpg"));
    }
    
    @Test
    public void shouldEvaluateBareUriAdaptation() throws Exception {
        String result = selectorFactory.get("#uri").withResultType(String.class).singleResult(journal);
        assertThat(result, equalTo("http://miskinhill.com.au/journals/test/"));
    }
    
    @Test
    public void shouldEvaluateUriSliceAdaptation() throws Exception {
        String result = selectorFactory.get("dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)")
                .withResultType(String.class).singleResult(journal);
        assertThat(result, equalTo("12345678"));
    }
    
    @Test
    public void shouldEvaluateSubscript() throws Exception {
        String result = selectorFactory.get(
                "!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)[0]/mhs:coverThumbnail#uri")
                .withResultType(String.class).singleResult(journal);
        assertThat(result, equalTo("http://miskinhill.com.au/journals/test/2:1/cover.thumb.jpg"));
        result = selectorFactory.get(
                "!mhs:isIssueOf(mhs:publicationDate#comparable-lv)[0]/mhs:coverThumbnail#uri")
                .withResultType(String.class).singleResult(journal);
        assertThat(result, equalTo("http://miskinhill.com.au/journals/test/1:1/cover.thumb.jpg"));
    }
    
    @Test
    public void shouldEvaluateLVAdaptation() throws Exception {
        List<Object> results = selectorFactory.get("dc:language/lingvoj:iso1#lv")
                .withResultType(Object.class).result(journal);
        assertThat(results.size(), equalTo(2));
        assertThat(results, hasItems((Object) "en", (Object) "ru"));
    }
    
    @Test
    public void shouldEvaluateTypePredicate() throws Exception {
        List<RDFNode> results = selectorFactory.get("!dc:creator[type=mhs:Review]")
                .withResultType(RDFNode.class).result(author);
        assertThat(results.size(), equalTo(1));
        assertThat(results, hasItems((RDFNode) review));
    }
    
    @Test
    public void shouldEvaluateAndCombinationOfPredicates() throws Exception {
        List<RDFNode> results = selectorFactory.get("!dc:creator[type=mhs:Article and uri-prefix='http://miskinhill.com.au/journals/']")
                .withResultType(RDFNode.class).result(author);
        assertThat(results.size(), equalTo(1));
        assertThat(results, hasItems((RDFNode) article));
    }
    
    @Test
    public void shouldEvaluateUnion() throws Exception {
        List<RDFNode> results = selectorFactory.get("!dc:creator | !mhs:translator")
                .withResultType(RDFNode.class).result(anotherAuthor);
        assertThat(results.size(), equalTo(3));
        assertThat(results, hasItems((RDFNode) article, (RDFNode) citedArticle, (RDFNode) anotherReview));
    }
    
}
