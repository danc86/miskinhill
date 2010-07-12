package au.com.miskinhill.rdf;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.SIOC;

@Component
public class AtomRepresentation implements Representation {

    private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.HTMLAnchors, ShownIn.HTMLLinks, ShownIn.Unapi);
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
        return MediaType.APPLICATION_ATOM_XML;
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
    public boolean isShownIn(ShownIn place) {
        return shownIn.contains(place);
    }

    @Override
    public String render(Resource resource) {
        return templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("template/atom/Forum.xml")),
                resource);
    }

}
