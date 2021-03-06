package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation.ShownIn;

@Component
public class RepresentationFactory {
    
    private final List<Representation> representations;
    private final Map<String, Representation> byFormat = new HashMap<String, Representation>();
    private final Map<MediaType, Representation> byContentType = new HashMap<MediaType, Representation>();
    
    @Autowired
    public RepresentationFactory(List<Representation> representations) {
        Collections.sort(representations, Representation.ORDER_COMPARATOR);
        this.representations = Collections.unmodifiableList(representations);
        
        for (Representation representation: representations) {
            byFormat.put(representation.getFormat(), representation);
            byContentType.put(representation.getContentType(), representation);
            for (MediaType alias: representation.getContentTypeAliases())
                byContentType.put(alias, representation);
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
    
    public List<Representation> getRepresentationsForResource(final Resource resource, final ShownIn place) {
        List<Representation> result = new ArrayList<Representation>();
        for (Representation representation: representations) {
            if (representation.canRepresent(resource) && representation.isShownIn(place))
                result.add(representation);
        }
        return result;
    }
    
    public Representation getRepresentationByFormat(String format) {
        return byFormat.get(format);
    }
    
    public Representation getRepresentationByContentType(MediaType contentType) {
        return byContentType.get(contentType);
    }
    
    public Set<String> getAllFormats() {
        return Collections.unmodifiableSet(byFormat.keySet());
    }
    
    public Collection<Representation> getAll() {
        return representations;
    }

    public List<Representation> getAllRepresentationsShownIn(ShownIn place) {
        List<Representation> result = new ArrayList<Representation>();
        for (Representation representation: representations) {
            if (representation.isShownIn(place))
                result.add(representation);
        }
        return result;
    }

}
