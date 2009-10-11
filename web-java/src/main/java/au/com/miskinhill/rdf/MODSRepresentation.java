package au.com.miskinhill.rdf;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdftemplate.TemplateInterpolator;

@Component
public class MODSRepresentation implements Representation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "mods+xml");
    private final Map<Resource, String> typeTemplates = new HashMap<Resource, String>();
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public MODSRepresentation(TemplateInterpolator templateInterpolator) {
        this.templateInterpolator = templateInterpolator;
        
        typeTemplates.put(MHS.Journal, "template/mods/Journal.xml");
        typeTemplates.put(MHS.Article, "template/mods/Article.xml");
    }
    
    @Override
    public boolean canRepresent(Resource resource) {
        return resource.getURI().startsWith("http://miskinhill.com.au/journals/") &&
                RDFUtil.hasAnyType(resource, typeTemplates.keySet());
    }

    @Override
    public MediaType getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public String getFormat() {
        return "mods";
    }
    
    @Override
    public String getLabel() {
        return "MODS";
    }
    
    @Override
    public String getDocs() {
        return "http://www.loc.gov/standards/mods/mods-userguide.html";
    }

    @Override
    public String render(Resource resource) {
        for (Resource type: RDFUtil.getTypes(resource)) {
            String templatePath = typeTemplates.get(type);
            if (templatePath != null) {
                return templateInterpolator.interpolate(
                        new InputStreamReader(this.getClass().getResourceAsStream(templatePath)),
                        resource);
            }
        }
        throw new IllegalArgumentException("No template found for " + resource);
    }

}
