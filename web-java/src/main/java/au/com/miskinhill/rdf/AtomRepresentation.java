package au.com.miskinhill.rdf;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.vocabulary.SIOC;
import au.com.miskinhill.rdftemplate.TemplateInterpolator;

@Component
public class AtomRepresentation implements Representation {
    
    private final Set<Resource> types = Collections.singleton(SIOC.Forum);
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public AtomRepresentation(TemplateInterpolator templateInterpolator) {
        this.templateInterpolator = templateInterpolator;
    }

    @Override
    public boolean canRepresent(Resource resource) {
        return RDFUtil.hasAnyType(resource, types);
    }

    @Override
    public MediaType getContentType() {
        return MediaType.APPLICATION_ATOM_XML_TYPE;
    }
    
    @Override
    public String getFormat() {
        return "atom";
    }
    
    @Override
    public int getOrder() {
        return 7;
    }
    
    @Override
    public String getLabel() {
        return "Atom";
    }
    
    @Override
    public String getDocs() {
        return "http://www.ietf.org/rfc/rfc4287.txt";
    }

    @Override
    public String render(Resource resource) {
        return templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("template/atom/Forum.xml")),
                resource);
    }

}
