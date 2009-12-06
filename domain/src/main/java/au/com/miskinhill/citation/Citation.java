package au.com.miskinhill.citation;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.XPath;

import au.com.miskinhill.rdf.vocabulary.MHS;

public class Citation {
    
    private static final URI CITED_BASE = URI.create("http://miskinhill.com.au/cited/");
    private static final XPath CITES_XPATH = DocumentFactory.getInstance().createXPath(".//*[" + containingClass("cites") + "]");
    private static final XPath CITATION_XPATH = DocumentFactory.getInstance().createXPath(".//*[" + containingClass("citation") + "]");
    private static final String[] OPENURL_FIELDS = {
        "atitle", "jtitle", "btitle", "date", "volume", "issue", "spage", "epage", "issn", "isbn", "au", "place", "pub", "edition"
    };
    private static final Map<String, XPath> OPENURL_FIELD_XPATHS = new HashMap<String, XPath>();
    static {
        for (String field: OPENURL_FIELDS)
            OPENURL_FIELD_XPATHS.put(field, DocumentFactory.getInstance().createXPath(".//*[" + containingClass(field) + "]"));
    }
    
    @SuppressWarnings("unchecked")
    public static Citation fromElement(URI articleUri, int number, Element citation) {
        Set<URI> cites = new HashSet<URI>();
        for (Element citesElement: (List<Element>) CITES_XPATH.selectNodes(citation))
            cites.add(CITED_BASE.resolve(citesElement.attributeValue("title")));
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, XPath> entry: OPENURL_FIELD_XPATHS.entrySet()) {
            List<Element> elements = entry.getValue().selectNodes(citation);
            if (!elements.isEmpty()) {
                List<String> values = new ArrayList<String>();
                for (Element element: elements)
                    values.add(normalizeSpace(titleOrText(element)));
                openurlFields.put(entry.getKey(), values);
            }
        }
        Genre genre = null;
        for (Genre possibleGenre: Genre.values()) {
            if (hasClass(citation, possibleGenre.name()))
                genre = possibleGenre;
        }
        return new Citation(articleUri, articleUri.resolve(String.format("#citation-%d", number)),
                cites, openurlFields, genre);
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
    private final Map<String, List<String>> openurlFields;
    private final Genre genre;
    
    protected Citation(URI articleUri, URI citationUri, Set<URI> cites, Map<String, List<String>> openurlFields, Genre genre) {
        this.articleUri = articleUri;
        this.citationUri = citationUri;
        this.cites = cites;
        this.openurlFields = openurlFields;
        this.genre = genre;
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
    
    public String coinsValue() {
        Map<String, List<String>> values = new LinkedHashMap<String, List<String>>();
        values.put("ctx_ver", Arrays.asList("Z39.88-2004"));
        values.put("rft.genre", Arrays.asList(genre.getCoinsGenre()));
        values.put("rft_val_format", Arrays.asList(genre.getCoinsFormat()));
        for (Map.Entry<String, List<String>> entry: openurlFields.entrySet())
            values.put("rft." + entry.getKey(), entry.getValue());
        
        List<String> pairs = new ArrayList<String>();
        try {
            for (Map.Entry<String, List<String>> valueEntry: values.entrySet())
                for (String value: valueEntry.getValue())
                    pairs.add(URLEncoder.encode(valueEntry.getKey(), "UTF-8") + "=" +
                            URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return StringUtils.join(pairs, "&");
    }
    
    public Set<URI> getCites() {
        return cites;
    }
    
    public List<String> getOpenurlField(String field) {
        return openurlFields.get(field);
    }
    
    public Genre getGenre() {
        return genre;
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
    
    /** Exposed for testing. */
    static String normalizeSpace(String s) {
        return s.replaceAll("[\\s\\p{Zs}]+", " ");
    }
    
    private static boolean hasClass(Element e, String clazz) {
        return normalizeSpace(" " + e.attributeValue("class") + " ").contains(" " + clazz + " ");
    }
    
    private static String titleOrText(Element e) {
        String title = e.attributeValue("title");
        if (title != null)
            return title;
        return e.getStringValue();
    }

}
