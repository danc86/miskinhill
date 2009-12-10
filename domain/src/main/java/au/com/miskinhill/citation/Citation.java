package au.com.miskinhill.citation;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.lang.StringUtils;

import au.com.miskinhill.rdf.vocabulary.MHS;

public class Citation {
    
    private static final URI CITED_BASE = URI.create("http://miskinhill.com.au/cited/");
    private static final QName CLASS_QNAME = new QName("class");
    private static final QName TITLE_QNAME = new QName("title");
    private static final String[] OPENURL_FIELDS = {
        "atitle", "jtitle", "btitle", "date", "volume", "issue", "spage", "epage", "issn", "isbn", "au", "place", "pub", "edition"
    };
    
    private static Citation fromTree(URI articleUri, int number, List<XMLEvent> events) {
        Genre genre = null;
        for (Genre possibleGenre: Genre.values()) {
            if (hasClass(events.get(0).asStartElement(), possibleGenre.name()))
                genre = possibleGenre;
        }
        
        Set<URI> cites = new HashSet<URI>();
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        int depth = 1;
        for (int i = 0; i < events.size(); i ++) {
            XMLEvent event = events.get(i);
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    depth ++;
                    StartElement start = event.asStartElement();
                    if (hasClass(start, "cites"))
                        cites.add(CITED_BASE.resolve(start.getAttributeByName(TITLE_QNAME).getValue()));
                    for (String field: OPENURL_FIELDS) {
                        if (hasClass(start, field)) {
                            String value;
                            Attribute titleAttribute = start.getAttributeByName(TITLE_QNAME);
                            if (titleAttribute != null) {
                                value = titleAttribute.getValue();
                            } else {
                                value = charactersFromTree(events.subList(i + 1, events.size()).iterator());
                            }
                            if (!openurlFields.containsKey(field))
                                openurlFields.put(field, new ArrayList<String>());
                            openurlFields.get(field).add(normalizeSpace(value));
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth --;
                    break;
                default:
                    // don't care
            }
        }
        
        return new Citation(articleUri, articleUri.resolve(String.format("#citation-%d", number)),
                cites, openurlFields, genre);
    }
    
    public static List<Citation> fromDocument(URI articleUri, Iterator<XMLEvent> reader) {
        List<Citation> citations = new ArrayList<Citation>();
        int i = 0;
        while (reader.hasNext()) {
            XMLEvent event = reader.next();
            if (event.isStartElement()) {
                StartElement start = event.asStartElement();
                if (hasClass(start, "citation")) {
                    i ++;
                    List<XMLEvent> events = consumeTree(start, reader);
                    citations.add(fromTree(articleUri, i, events));
                }
            }
        }
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
    
    /** Exposed for testing. */
    static String normalizeSpace(String s) {
        return s.replaceAll("[\\s\\p{Zs}]+", " ");
    }
    
    private static boolean hasClass(StartElement e, String clazz) {
        Attribute classAttribute = e.getAttributeByName(CLASS_QNAME);
        if (classAttribute == null)
            return false;
        return normalizeSpace(" " + classAttribute.getValue() + " ").contains(" " + clazz + " ");
    }
    
    private static List<XMLEvent> consumeTree(StartElement start, Iterator<XMLEvent> reader) {
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        events.add(start);
        int depth = 1;
        while (depth > 0) {
            XMLEvent event = reader.next();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    depth ++;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth --;
                    break;
                default:
                    // don't care
            }
            events.add(event);
        }
        return events;
    }
    
    private static String charactersFromTree(Iterator<XMLEvent> reader) {
        int depth = 1;
        StringBuilder sb = new StringBuilder();
        while (depth > 0) {
            XMLEvent event = reader.next();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    depth ++;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth --;
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    sb.append(event.asCharacters().getData());
                    break;
                default:
                    // don't care
            }
        }
        return sb.toString();
    }
    
}
