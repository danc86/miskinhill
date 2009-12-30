package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.rdftemplate.XMLStream;
import au.com.miskinhill.rdftemplate.selector.Adaptation;

public class RepresentationAtomLinksAdaptation implements Adaptation<XMLStream> {
    
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final String XHTML_NS_URI = "http://www.w3.org/2005/Atom";
    private static final QName LINK_QNAME = new QName(XHTML_NS_URI, "link");
    
    @Override
    public XMLStream adapt(RDFNode node) {
        Resource resource = (Resource) node;
        List<Representation> representations = StaticApplicationContextAccessor.getBeanOfType(RepresentationFactory.class)
                .getRepresentationsForResource(resource);
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        for (Representation representation: representations) {
            HashSet<Attribute> attributes = new HashSet<Attribute>();
            attributes.add(eventFactory.createAttribute(new QName("rel"), "alternate"));
            attributes.add(eventFactory.createAttribute(new QName("type"), representation.getContentType().toString()));
            if (representation.getOrder() == 0)
                attributes.add(eventFactory.createAttribute(new QName("href"), resource.getURI()));
            else
                attributes.add(eventFactory.createAttribute(new QName("href"), resource.getURI() + "." + representation.getFormat()));
            events.add(eventFactory.createStartElement(LINK_QNAME, attributes.iterator(), null));
            events.add(eventFactory.createEndElement(LINK_QNAME, null));
        }
        return new XMLStream(events);
    }

    @Override
    public Class<XMLStream> getDestinationType() {
        return XMLStream.class;
    }

}
