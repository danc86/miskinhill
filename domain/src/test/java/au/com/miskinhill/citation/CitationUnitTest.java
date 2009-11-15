package au.com.miskinhill.citation;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class CitationUnitTest {
    
    @Test
    public void fromDocumentShouldFindAllCitations() throws Exception {
        List<Citation> citations = citationsFromDocument();
        assertThat(citations.size(), equalTo(7));
    }
    
    @Test
    public void toRDFShouldWork() throws Exception {
        Set<Statement> citationStmts = citationsFromDocument().get(2).toRDF();
        
        assertThat(citationStmts.size(), equalTo(3));
        Resource articleRes = ResourceFactory.createResource("http://miskinhill.com.au/journals/test/1:1/test-article");
        Resource citationRes = ResourceFactory.createResource("http://miskinhill.com.au/journals/test/1:1/test-article#citation-3");
        Resource citedRes = ResourceFactory.createResource("http://miskinhill.com.au/cited/books/olcott-2001");
        assertThat(citationStmts, hasItems(
                ResourceFactory.createStatement(citationRes, RDF.type, MHS.Citation),
                ResourceFactory.createStatement(citationRes, DCTerms.isPartOf, articleRes),
                ResourceFactory.createStatement(citationRes, MHS.cites, citedRes)));
    }

    private List<Citation> citationsFromDocument() throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new XhtmlEntityResolver());
        Document doc = reader.read(getClass().getResourceAsStream("citations.xml"));
        return Citation.fromDocument(URI.create("http://miskinhill.com.au/journals/test/1:1/test-article"), doc);
    }

}
