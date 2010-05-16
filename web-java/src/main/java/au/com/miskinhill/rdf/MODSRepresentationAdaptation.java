package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import au.id.djc.rdftemplate.XMLStream;
import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

public class MODSRepresentationAdaptation extends AbstractAdaptation<XMLStream, RDFNode> {
    
    private static final String MODS_NS = "http://www.loc.gov/mods/v3";
    private static final QName MODS_QNAME = new QName(MODS_NS, "mods");
    
    public MODSRepresentationAdaptation() {
        super(XMLStream.class, new Class<?>[] { }, RDFNode.class);
    }
    
    @Override
    protected XMLStream doAdapt(RDFNode node) {
        XMLStreamRepresentation modsRepresentation = null;
        for (XMLStreamRepresentation r: StaticApplicationContextAccessor.getBeansOfType(XMLStreamRepresentation.class)) {
            if (r.getFormat().equals("mods")) {
                modsRepresentation = r;
                break;
            }
        }
        Resource resource = node.as(Resource.class);
        if (!modsRepresentation.canRepresent(resource))
            throw new SelectorEvaluationException("Cannot represent node " + node + " as MODS");
        
        XMLStream stream = modsRepresentation.renderXMLStream(resource);
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        int depth = -1;
        for (XMLEvent event: stream) {
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (depth >= 0) {
                        depth ++;
                        events.add(event);
                    } else if (event.asStartElement().getName().equals(MODS_QNAME)) {
                        depth ++;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (depth > 0) {
                        depth --;
                        events.add(event);
                    } else if (depth == 0) {
                        depth --;
                    }
                    break;
                default:
                    if (depth >= 0) {
                        events.add(event);
                    }
            }
        }
        return new XMLStream(events);
    }

}
