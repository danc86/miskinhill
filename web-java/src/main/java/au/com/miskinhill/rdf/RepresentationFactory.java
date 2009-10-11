package au.com.miskinhill.rdf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepresentationFactory {
    
    private static RepresentationFactory instance;
    
    /** XXX lame */
    public static RepresentationFactory getInstance() {
        return instance;
    }
    
    private final Set<Representation> representations;
    private final Map<String, Representation> byFormat = new HashMap<String, Representation>();
    
    @Autowired
    public RepresentationFactory(Set<Representation> representations) {
        this.representations = representations;
        instance = this;
        
        for (Representation representation: representations) {
            byFormat.put(representation.getFormat(), representation);
        }
    }
    
    public Set<Representation> getRepresentationsForResource(final Resource resource) {
        Set<Representation> result = new HashSet<Representation>();
        for (Representation representation: representations) {
            if (representation.canRepresent(resource))
                result.add(representation);
        }
        return result;
    }
    
    public Representation getRepresentationByFormat(String format) {
        return byFormat.get(format);
    }

}
