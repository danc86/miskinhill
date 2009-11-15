package au.com.miskinhill.citation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.XPath;

import au.com.miskinhill.rdf.vocabulary.MHS;

public class Citation {
    
    private static final URI CITED_BASE = URI.create("http://miskinhill.com.au/cited/");
    private static final XPath CITES_XPATH = DocumentFactory.getInstance().createXPath(".//*[" + containingClass("cites") + "]");
    private static final XPath CITATION_XPATH = DocumentFactory.getInstance().createXPath(".//*[" + containingClass("citation") + "]");
    
    @SuppressWarnings("unchecked")
    public static Citation fromElement(URI articleUri, int number, Element citation) {
        Set<URI> cites = new HashSet<URI>();
        for (Element citesElement: (List<Element>) CITES_XPATH.selectNodes(citation))
            cites.add(CITED_BASE.resolve(citesElement.attributeValue("title")));
        return new Citation(articleUri, articleUri.resolve(String.format("#citation-%d", number)), cites);
    }
    
    @SuppressWarnings("unchecked")
    public static List<Citation> fromDocument(URI articleUri, Document content) {
        List<Citation> citations = new ArrayList<Citation>();
        List<Element> citationElements = CITATION_XPATH.selectNodes(content);
        for (int i = 1; i <= citationElements.size(); i++)
            citations.add(fromElement(articleUri, i, citationElements.get(i - 1)));
        return citations;
    }

    private final URI articleUri;
    private final URI citationUri;
    private final Set<URI> cites;
    
    private Citation(URI articleUri, URI citationUri, Set<URI> cites) {
        this.articleUri = articleUri;
        this.citationUri = citationUri;
        this.cites = cites;
    }
    
    public Set<Statement> toRDF() {
        Set<Statement> stmts = new HashSet<Statement>(Arrays.asList(
                stmt(citationUri, RDF.type, MHS.Citation),
                stmt(citationUri, DCTerms.isPartOf, articleUri)));
        for (URI cited: cites) {
            stmts.add(stmt(citationUri, MHS.cites, cited));
        }
        return stmts;
    }
    
    private Statement stmt(URI subject, Property predicate, Resource object) {
        return ResourceFactory.createStatement(ResourceFactory.createResource(subject.toString()), predicate, object);
    }
    
    private Statement stmt(URI subject, Property predicate, URI object) {
        return stmt(subject, predicate, ResourceFactory.createResource(object.toString()));
    }
    
    private static String containingClass(String className) {
        return "contains(concat(' ',normalize-space(@class),' '),' " + className + " ')";
    }

}
