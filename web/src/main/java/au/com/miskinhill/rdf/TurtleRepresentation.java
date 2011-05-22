package au.com.miskinhill.rdf;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TurtleRepresentation extends AbstractRDFRepresentation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "x-turtle");
    
    @Autowired
    public TurtleRepresentation(Model bareModel) {
        super(bareModel);
    }

    @Override
    public boolean canRepresent(Resource resource) {
        return true;
    }

    @Override
    public MediaType getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public Collection<MediaType> getContentTypeAliases() {
        return Collections.emptySet();
    }

    @Override
    public String getDocs() {
        return "http://www.w3.org/TR/REC-rdf-syntax/";
    }

    @Override
    public String getFormat() {
        return "ttl";
    }
    
    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getLabel() {
        return "Turtle";
    }

    @Override
    public String renderSubgraph(Model subgraph) {
        StringWriter w = new StringWriter();
        subgraph.write(w, "TURTLE");
        return w.toString();
    }

}
