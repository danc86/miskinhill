package au.com.miskinhill.rdf;

import java.util.Comparator;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.http.MediaType;

public interface Representation {
    
    public static final Comparator<Representation> ORDER_COMPARATOR = new Comparator<Representation>() {
        @Override
        public int compare(Representation left, Representation right) {
            return left.getOrder() - right.getOrder();
        }
    };
    
    boolean canRepresent(Resource resource);
    
    MediaType getContentType();
    
    String getFormat();
    
    int getOrder();
    
    String getLabel();
    
    /** URL of a document describing the format. Required by unapi. */
    String getDocs();
    
    String render(Resource resource);

}
