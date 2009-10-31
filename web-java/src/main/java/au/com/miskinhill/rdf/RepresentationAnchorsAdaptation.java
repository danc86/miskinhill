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

public class RepresentationAnchorsAdaptation implements Adaptation<XMLStream> {
    
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    private static final QName A_QNAME = new QName(XHTML_NS_URI, "a");
    
    @Override
    public XMLStream adapt(RDFNode node) {
        Resource resource = (Resource) node;
        List<Representation> representations = RepresentationFactory.getInstance().getRepresentationsForResource(resource);
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        boolean first = true;
        for (Representation representation: representations) {
            if (representation.getFormat().equals("html"))
                continue;
            if (!first)
                events.add(eventFactory.createCharacters(", "));
            HashSet<Attribute> attributes = new HashSet<Attribute>();
            attributes.add(eventFactory.createAttribute(new QName("rel"), "alternate"));
            attributes.add(eventFactory.createAttribute(new QName("href"), resource.getURI() + "." + representation.getFormat()));
            events.add(eventFactory.createStartElement(A_QNAME, attributes.iterator(), null));
            events.add(eventFactory.createCharacters(representation.getLabel()));
            events.add(eventFactory.createEndElement(A_QNAME, null));
            first = false;
        }
        return new XMLStream(events);
    }

    @Override
    public Class<XMLStream> getDestinationType() {
        return XMLStream.class;
    }

}
