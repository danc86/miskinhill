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
public class RDFXMLRepresentation extends AbstractRDFRepresentation {
    
    private final MediaType contentType = new MediaType("application", "rdf+xml");
    
    @Autowired
    public RDFXMLRepresentation(Model bareModel) {
        super(bareModel);
    }

    @Override
    public boolean canRepresent(Resource resource) {
        return true;
    }

    @Override
    public MediaType getContentType() {
        return contentType;
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
    public String renderSubgraph(Model subgraph) {
        StringWriter w = new StringWriter();
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        subgraph.write(w, "RDF/XML-ABBREV");
        return w.toString();
    }

}
