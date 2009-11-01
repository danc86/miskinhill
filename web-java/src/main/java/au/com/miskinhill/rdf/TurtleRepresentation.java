package au.com.miskinhill.rdf;

import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class TurtleRepresentation implements Representation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "x-turtle");

    @Override
    public boolean canRepresent(Resource resource) {
        return true;
    }

    @Override
    public MediaType getContentType() {
        return CONTENT_TYPE;
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
    public String getLabel() {
        return "Turtle";
    }

    @Override
    public String render(Resource resource) {
        SubgraphAccumulator acc = new SubgraphAccumulator(resource.getModel());
        acc.visit(resource);
        // XXX should also do defragged
        StringWriter w = new StringWriter();
        acc.getSubgraph().write(w, "TURTLE");
        return w.toString();
    }

}
