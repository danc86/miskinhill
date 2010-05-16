package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Resource;

import au.id.djc.rdftemplate.XMLStream;
import au.id.djc.rdftemplate.selector.AbstractAdaptation;

public class RepresentationLinksAdaptation extends AbstractAdaptation<XMLStream, Resource> {
    
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    private static final QName LINK_QNAME = new QName(XHTML_NS_URI, "link");
    
    public RepresentationLinksAdaptation() {
        super(XMLStream.class, new Class<?>[] { }, Resource.class);
    }
    
    @Override
    protected XMLStream doAdapt(Resource resource) {
        List<Representation> representations = StaticApplicationContextAccessor.getBeanOfType(RepresentationFactory.class)
                .getRepresentationsForResource(resource);
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        for (Representation representation: representations) {
            if (representation.getFormat().equals("html"))
                continue;
            HashSet<Attribute> attributes = new HashSet<Attribute>();
            attributes.add(eventFactory.createAttribute(new QName("rel"), "alternate"));
            attributes.add(eventFactory.createAttribute(new QName("type"), representation.getContentType().toString()));
            attributes.add(eventFactory.createAttribute(new QName("title"), representation.getLabel()));
            attributes.add(eventFactory.createAttribute(new QName("href"), resource.getURI() + "." + representation.getFormat()));
            events.add(eventFactory.createStartElement(LINK_QNAME, attributes.iterator(), null));
            events.add(eventFactory.createEndElement(LINK_QNAME, null));
        }
        return new XMLStream(events);
    }

}
