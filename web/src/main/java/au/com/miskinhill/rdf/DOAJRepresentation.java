package au.com.miskinhill.rdf;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;
import au.id.djc.rdftemplate.XMLStream;
import au.id.djc.rdftemplate.selector.SelectorFactory;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
public class DOAJRepresentation implements XMLStreamRepresentation {
    
    protected static final QName RECORDS = new QName("", "records");
    protected static final QName TITLE = new QName("", "title");
    protected static final QName LANGUAGE = new QName("", "language");
    protected static final QName XML_LANG = new QName(XMLConstants.XML_NS_URI, "lang");
    
    private final MediaType contentType = new MediaType("application", "doaj+xml");
    private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.Unapi);
    private final URI xsd = URI.create("http://www.doaj.org/schemas/doajArticles.xsd");
    private final TemplateInterpolator templateInterpolator;
    private final SelectorFactory selectorFactory;
    private final XMLEventFactory eventFactory;
    private final XMLOutputFactory outputFactory;
    
    @Autowired
    public DOAJRepresentation(TemplateInterpolator templateInterpolator,
            SelectorFactory selectorFactory, XMLEventFactory eventFactory,
            XMLOutputFactory outputFactory) {
        this.templateInterpolator = templateInterpolator;
        this.selectorFactory = selectorFactory;
        this.eventFactory = eventFactory;
        this.outputFactory = outputFactory;
    }
    
    @Override
    public boolean canRepresent(Resource resource) {
        return resource.getURI().startsWith("http://miskinhill.com.au/journals/") &&
                resource.hasProperty(RDF.type, MHS.Journal);
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }
    
    @Override
    public Collection<MediaType> getContentTypeAliases() {
        return Collections.emptySet();
    }
    
    @Override
    public String getFormat() {
        return "doaj";
    }
    
    @Override
    public int getOrder() {
        return 9;
    }
    
    @Override
    public String getLabel() {
        return "DOAJ article XML format";
    }
    
    @Override
    public String getDocs() {
        return "http://www.doaj.org/doaj?func=loadTempl&templ=070507";
    }
    
    @Override
    public boolean isShownIn(ShownIn place) {
        return shownIn.contains(place);
    }
    
    @Override
    public URI getXMLNamespace() {
        return null;
    }
    
    @Override
    public URI getXSD() {
        return xsd;
    }

    @Override
    public String render(Resource resource) {
        StringWriter writer = new StringWriter();
        final XMLEventWriter eventWriter;
        try {
            eventWriter = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        XMLEventConsumer destination = new XMLEventConsumer() {
            @Override
            public void add(XMLEvent event) throws XMLStreamException {
                eventWriter.add(event);
            }
        };
        try {
            render(resource, destination);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }
    
    @Override
    public XMLStream renderXMLStream(Resource resource) {
        final ArrayList<XMLEvent> events = new ArrayList<XMLEvent>();
        try {
            render(resource, new XMLEventConsumer() {
                @Override
                public void add(XMLEvent event) throws XMLStreamException {
                    events.add(event);
                }
            });
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return new XMLStream(events);
    }
    
    private void render(Resource resource, XMLEventConsumer destination) throws XMLStreamException {
        if (!resource.hasProperty(RDF.type, MHS.Journal))
            throw new IllegalArgumentException("Cannot represent " + resource);
        destination.add(eventFactory.createStartDocument());
        destination.add(eventFactory.createStartElement(RECORDS, null, null));
        for (RDFNode article:
                selectorFactory.get("!mhs:isIssueOf/!dc:isPartOf[type=mhs:Article]")
                    .withResultType(RDFNode.class).result(resource)) {
            templateInterpolator.interpolate(
                    this.getClass().getResourceAsStream("template/doaj/Article.xml"),
                    article, new XMLEventFilter(destination));
        }
        destination.add(eventFactory.createEndElement(RECORDS, null));
        destination.add(eventFactory.createEndDocument());
    }
    
    private final class XMLEventFilter implements XMLEventConsumer {
        
        private final XMLEventConsumer destination;
        
        public XMLEventFilter(XMLEventConsumer destination) {
            this.destination = destination;
        }
        
        @Override
        public void add(XMLEvent event) throws XMLStreamException {
            switch (event.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                case XMLEvent.END_DOCUMENT:
                    // discard
                    break;
                case XMLEvent.START_ELEMENT:
                    StartElement start = event.asStartElement();
                    if (start.getAttributeByName(XML_LANG) != null) {
                        StartElement cloned = eventFactory.createStartElement(
                                start.getName().getPrefix(),
                                start.getName().getNamespaceURI(),
                                start.getName().getLocalPart(),
                                fixAttributes(start).iterator(),
                                start.getNamespaces(),
                                start.getNamespaceContext());
                        start = cloned;
                    }
                    destination.add(start);
                    break;
                default:
                    destination.add(event);
            }
        }

        @SuppressWarnings("unchecked")
        protected Set<Attribute> fixAttributes(StartElement start) {
            Set<Attribute> attributes = new LinkedHashSet<Attribute>();
            for (Iterator<Attribute> it = start.getAttributes(); it.hasNext(); ) {
                Attribute attribute = it.next();
                if (attribute.getName().equals(XML_LANG)) {
                    // convert to language if accepted, otherwise drop
                    if (start.getName().equals(TITLE)) {
                        attributes.add(eventFactory.createAttribute(LANGUAGE,
                                attribute.getValue()));
                    }
                } else {
                    attributes.add(attribute);
                }
            }
            return attributes;
        }
        
    }

}
