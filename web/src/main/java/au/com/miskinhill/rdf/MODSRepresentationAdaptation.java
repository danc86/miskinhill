package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.XMLStream;
import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MODSRepresentationAdaptation extends AbstractAdaptation<XMLStream, Resource> {
    
    private static final String MODS_NS = "http://www.loc.gov/mods/v3";
    private static final QName MODS_QNAME = new QName(MODS_NS, "mods");
    
    private final XMLStreamRepresentation modsRepresentation;
    
    @Autowired
    public MODSRepresentationAdaptation(RepresentationFactory representationFactory) {
        super(XMLStream.class, new Class<?>[] { }, Resource.class);
        this.modsRepresentation = (XMLStreamRepresentation) representationFactory.getRepresentationByFormat("mods");
    }
    
    @Override
    protected XMLStream doAdapt(Resource resource) {
        if (!modsRepresentation.canRepresent(resource))
            throw new SelectorEvaluationException("Cannot represent node " + resource + " as MODS");
        
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
