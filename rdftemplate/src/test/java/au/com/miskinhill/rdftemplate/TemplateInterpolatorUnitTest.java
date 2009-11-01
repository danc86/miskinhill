package au.com.miskinhill.rdftemplate;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.miskinhill.rdftemplate.datatype.DateDataType;
import au.com.miskinhill.rdftemplate.selector.AntlrSelectorFactory;

public class TemplateInterpolatorUnitTest {
    
    @BeforeClass
    public static void ensureDatatypesRegistered() {
        DateDataType.registerStaticInstance();
    }
    
    private Model model;
    private TemplateInterpolator templateInterpolator;
    
    @Before
    public void setUp() {
        model = ModelFactory.createDefaultModel();
        InputStream stream = this.getClass().getResourceAsStream(
                "/au/com/miskinhill/rdftemplate/test-data.xml");
        model.read(stream, "");
        templateInterpolator = new TemplateInterpolator(new AntlrSelectorFactory());
    }
    
    @Test
    public void shouldReplaceSubtreesWithContent() throws Exception {
        Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
        String result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("replace-subtree.xml")), journal);
        assertThat(result, containsString("<div xml:lang=\"en\" lang=\"en\">Test Journal of Good Stuff</div>"));
        assertThat(result, not(containsString("<p>This should all go <em>away</em>!</p>")));
    }
    
    @Test
    public void shouldHandleXMLLiterals() throws Exception {
        Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
        String result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("replace-xml.xml")), journal);
        assertThat(result, containsString(
                "<div xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><p><em>Test Journal</em> is a journal.</p></div>"));
    }
    
    @Test
    public void shouldHandleIfs() throws Exception {
        Resource author = model.getResource("http://miskinhill.com.au/authors/test-author");
        String result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("conditional.xml")), author);
        assertThat(result, containsString("attribute test"));
        assertThat(result, containsString("element test"));
        assertThat(result, not(containsString("rdf:if")));
        
        Resource authorWithoutNotes = model.getResource("http://miskinhill.com.au/authors/another-author");
        result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("conditional.xml")), authorWithoutNotes);
        assertThat(result, not(containsString("attribute test")));
        assertThat(result, not(containsString("element test")));
    }
    
    @Test
    public void shouldHandleJoins() throws Exception {
        Resource citedArticle = model.getResource("http://miskinhill.com.au/cited/journals/asdf/1:1/article");
        String result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("join.xml")), citedArticle);
        assertThat(result, containsString("<p><a href=\"http://miskinhill.com.au/authors/another-author\">Another Author</a>, " +
                "<a href=\"http://miskinhill.com.au/authors/test-author\">Test Author</a></p>"));
    }
    
    @Test
    public void shouldHandleFor() throws Exception {
        Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
        String result = templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("for.xml")), journal);
        assertThat(result, containsString("<span>http://miskinhill.com.au/journals/test/1:1/</span>"));
        assertThat(result, containsString("<span>http://miskinhill.com.au/journals/test/2:1/</span>"));
        assertThat(result, containsString("<p>http://miskinhill.com.au/journals/test/1:1/</p>"));
        assertThat(result, containsString("<p>http://miskinhill.com.au/journals/test/2:1/</p>"));
    }
    
}
