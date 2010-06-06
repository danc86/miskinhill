package au.com.miskinhill.web.feeds;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.commons.codec.CharEncoding;

import org.apache.commons.codec.net.URLCodec;

import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.collections15.IteratorUtils;
import org.joda.time.DateTime;

import au.id.djc.rdftemplate.TemplateInterpolator;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

import au.com.miskinhill.rdf.RDFUtil;
import au.com.miskinhill.rdf.vocabulary.MHS;

public abstract class AbstractAtomFeedResource {
    
    protected static final String ATOM_NS = "http://www.w3.org/2005/Atom";
    protected static final QName FEED_QNAME = new QName(ATOM_NS, "feed");
    protected static final QName TITLE_QNAME = new QName(ATOM_NS, "title");
    protected static final QName ID_QNAME = new QName(ATOM_NS, "id");
    protected static final QName LINK_QNAME = new QName(ATOM_NS, "link");
    protected static final QName PUBLISHED_QNAME = new QName(ATOM_NS, "published");
    protected static final QName UPDATED_QNAME = new QName(ATOM_NS, "updated");
    
    /** Ugh */
    protected static final class ProperURLCodec extends URLCodec {
        private static final BitSet SAFE = (BitSet) WWW_FORM_URL.clone();
        static { SAFE.clear(' '); }
        private static final Charset UTF8 = Charset.forName(CharEncoding.UTF_8);
        private static final Charset ASCII = Charset.forName(CharEncoding.US_ASCII);
        public static String encodeUrl(String raw) {
            return new String(encodeUrl(SAFE, raw.getBytes(UTF8)), ASCII);
        }
    }
    
    private final Map<Resource, String> typeTemplates = new HashMap<Resource, String>();
    protected final TemplateInterpolator templateInterpolator;
    protected final XMLEventFactory eventFactory;
    
    public AbstractAtomFeedResource(TemplateInterpolator templateInterpolator, XMLEventFactory eventFactory) {
        this.templateInterpolator = templateInterpolator;
        this.eventFactory = eventFactory;
        typeTemplates.put(MHS.Issue, "/au/com/miskinhill/rdf/template/atomfragment/Issue.xml");
    }
    
    protected void addTitleElement(List<XMLEvent> destination, String title) {
        destination.add(eventFactory.createStartElement(TITLE_QNAME,
                IteratorUtils.singletonIterator(eventFactory.createAttribute("type", "text")), null));
        destination.add(eventFactory.createCharacters(title));
        destination.add(eventFactory.createEndElement(TITLE_QNAME, null));
    }
    
    protected void addIdAndSelfLinkElements(List<XMLEvent> destination, String url) {
        destination.add(eventFactory.createStartElement(ID_QNAME, null, null));
        destination.add(eventFactory.createCharacters(url));
        destination.add(eventFactory.createEndElement(ID_QNAME, null));
        destination.add(eventFactory.createStartElement(LINK_QNAME, Arrays.asList(
                eventFactory.createAttribute("rel", "self"),
                eventFactory.createAttribute("type", "application/atom+xml"),
                eventFactory.createAttribute("href", url)
                ).iterator(), null));
        destination.add(eventFactory.createEndElement(LINK_QNAME, null));
    }
    
    protected void addUpdated(List<XMLEvent> destination, DateTime updated) {
        destination.add(eventFactory.createStartElement(UPDATED_QNAME, null, null));
        destination.add(eventFactory.createCharacters(updated.toString()));
        destination.add(eventFactory.createEndElement(UPDATED_QNAME, null));
    }
    
    protected AtomEntry renderEntry(Resource resource) {
        String templatePath = null;
        for (Resource type: RDFUtil.getTypes(resource)) {
            templatePath = typeTemplates.get(type);
            if (templatePath != null) break;
        }
        if (templatePath == null)
            throw new SelectorEvaluationException("No Atom entry fragment template found for node " + resource);
        
        final List<XMLEvent> events = new ArrayList<XMLEvent>();
        final StringBuilder published = new StringBuilder();
        final StringBuilder updated = new StringBuilder();
        XMLEventConsumer destination = new XMLEventConsumer() {
            private boolean inPublished = false;
            private boolean inUpdated = false;
            @Override
            public void add(XMLEvent event) throws XMLStreamException {
                events.add(event);
                if (inPublished && event.isCharacters())
                    published.append(event.asCharacters().getData());
                else if (inPublished && event.isEndElement())
                    inPublished = false;
                else if (inUpdated && event.isCharacters())
                    updated.append(event.asCharacters().getData());
                else if (inUpdated && event.isEndElement())
                    inUpdated = false;
                else if (event.isStartElement() && event.asStartElement().getName().equals(PUBLISHED_QNAME))
                    inPublished = true;
                else if (event.isStartElement() && event.asStartElement().getName().equals(UPDATED_QNAME))
                    inUpdated = true;
            }
        };
        templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream(templatePath)),
                resource, destination);
        if (events.get(0).isStartDocument() && events.get(events.size() - 1).isEndDocument()) {
            events.remove(events.size() - 1);
            events.remove(0);
        }
        return new AtomEntry(events, new DateTime(published.toString()), new DateTime(updated.toString()));
    }

}
