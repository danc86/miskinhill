package au.com.miskinhill.rdf;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Representation {
    
    boolean canRepresent(Resource resource);
    
    MediaType getContentType();
    
    String getFormat();
    
    int getOrder();
    
    String getLabel();
    
    /** URL of a document describing the format. Required by unapi. */
    String getDocs();
    
    String render(Resource resource);

}
