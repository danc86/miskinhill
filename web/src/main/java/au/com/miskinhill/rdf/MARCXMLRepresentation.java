package au.com.miskinhill.rdf;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
public class MARCXMLRepresentation implements Representation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "marcxml+xml");
    private final Set<Resource> types = Collections.singleton(MHS.Journal);
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public MARCXMLRepresentation(TemplateInterpolator templateInterpolator) {
        this.templateInterpolator = templateInterpolator;
    }

    @Override
    public boolean canRepresent(Resource resource) {
        return resource.getURI().startsWith("http://miskinhill.com.au/journals/") && RDFUtil.hasAnyType(resource, types);
    }

    @Override
    public MediaType getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public String getFormat() {
        return "marcxml";
    }
    
    @Override
    public int getOrder() {
        return 4;
    }
    
    @Override
    public String getLabel() {
        return "MARCXML";
    }
    
    @Override
    public String getDocs() {
        return "http://www.loc.gov/standards/marcxml/";
    }

    @Override
    public String render(Resource resource) {
        return templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("template/marcxml/Journal.xml")),
                resource);
    }

}