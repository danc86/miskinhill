package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

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
    
    private final List<Representation> representations;
    private final Map<String, Representation> byFormat = new HashMap<String, Representation>();
    private final Map<MediaType, Representation> byContentType = new HashMap<MediaType, Representation>();
    
    @Autowired
    public RepresentationFactory(List<Representation> representations) {
        Collections.sort(representations, new AnnotationAwareOrderComparator()); // why doesn't spring do this for me?
        this.representations = Collections.unmodifiableList(representations);
        instance = this;
        
        for (Representation representation: representations) {
            byFormat.put(representation.getFormat(), representation);
            byContentType.put(representation.getContentType(), representation);
        }
    }
    
    public List<Representation> getRepresentationsForResource(final Resource resource) {
        List<Representation> result = new ArrayList<Representation>();
        for (Representation representation: representations) {
            if (representation.canRepresent(resource))
                result.add(representation);
        }
        return result;
    }
    
    public Representation getRepresentationByFormat(String format) {
        return byFormat.get(format);
    }
    
    public Representation getRepresentationByContentType(String contentType) {
        return byContentType.get(MediaType.valueOf(contentType));
    }
    
    public Set<String> getAllFormats() {
        return byFormat.keySet();
    }
    
    public Collection<Representation> getAll() {
        return representations;
    }

}
