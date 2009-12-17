package au.com.miskinhill.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;

import au.com.miskinhill.rdftemplate.XMLStream;
import au.com.miskinhill.rdftemplate.selector.Adaptation;
import au.com.miskinhill.rdftemplate.selector.SelectorFactory;

public class ArticleLinksAdaptation implements Adaptation<XMLStream> {
    
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    private static final QName A_QNAME = new QName(XHTML_NS_URI, "a");
    private static final QName HREF_QNAME = new QName("href");
    
    @Override
    public XMLStream adapt(RDFNode node) {
        List<Link> links = new ArrayList<Link>();
        SelectorFactory selectorFactory = StaticApplicationContextAccessor.getBeanOfType(SelectorFactory.class);
        String title = oneOrNull(selectorFactory.get("dc:title#string-lv").withResultType(String.class).result(node));
        String issn = oneOrNull(selectorFactory.get("dc:isPartOf/mhs:isIssueOf/dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)")
                .withResultType(String.class).result(node));
        
        if (title != null) {
            links.add(new Link("http://scholar.google.com.au/scholar?q=" + query(title, "UTF-8"), "Google Scholar"));
        }
        
        if (issn != null) {
            links.add(new Link(
                    String.format("http://www.worldcat.org/search?q=issn:%s-%s", issn.substring(0, 4), issn.substring(4)),
                    "WorldCat"));
        }
        
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
            Link link = it.next();
            link.write(events);
            if (it.hasNext())
                events.add(eventFactory.createCharacters(",\n"));
        }
        return new XMLStream(events);
    }

    @Override
    public Class<XMLStream> getDestinationType() {
        return XMLStream.class;
    }
    
    private static final class Link {
        private final String href;
        private final String anchor;
        public Link(String href, String anchor) {
            this.href = href;
            this.anchor = anchor;
        }
        public void write(List<XMLEvent> destination) {
            Attribute hrefAttr = eventFactory.createAttribute(HREF_QNAME, href);
            destination.add(eventFactory.createStartElement(A_QNAME, Collections.singleton(hrefAttr).iterator(), null));
            destination.add(eventFactory.createCharacters(anchor));
            destination.add(eventFactory.createEndElement(A_QNAME, null));
        }
    }
    
    private String query(String raw, String encoding) {
        try {
            return URLEncoder.encode(raw, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private <T> T oneOrNull(List<T> ts) {
        switch (ts.size()) {
            case 0: return null;
            case 1: return ts.get(0);
            default: throw new RuntimeException("Got more than one thing: " + ts);
        }
    }

}