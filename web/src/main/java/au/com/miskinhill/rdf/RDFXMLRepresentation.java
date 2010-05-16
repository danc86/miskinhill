package au.com.miskinhill.rdf;

import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class RDFXMLRepresentation implements Representation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "rdf+xml");

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
        return "xml";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getLabel() {
        return "RDF/XML";
    }

    @Override
    public String render(Resource resource) {
        SubgraphAccumulator acc = new SubgraphAccumulator(resource.getModel());
        acc.visit(resource);
        // XXX should also do defragged
        StringWriter w = new StringWriter();
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        acc.getSubgraph().write(w, "RDF/XML-ABBREV");
        return w.toString();
    }

}
