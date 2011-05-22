package au.com.miskinhill.rdf;

import java.util.Collection;
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
    
    Collection<MediaType> getContentTypeAliases();
    
    String getFormat();
    
    int getOrder();
    
    String getLabel();
    
    /** URL of a document describing the format. Required by unapi. */
    String getDocs();
    
    public enum ShownIn {
        HTMLAnchors, HTMLLinks, Unapi, AtomLinks, OAIPMH
    }
    boolean isShownIn(ShownIn place);
    
    String render(Resource resource);

}
