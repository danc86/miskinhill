package au.com.miskinhill.web.rdf;

import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdftemplate.TemplateInterpolator;

@Component
@Path("/rdfschema/1.0/")
public class RDFSchemaResource {
    
    private final Model model;
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public RDFSchemaResource(Model model, TemplateInterpolator templateInterpolator) {
        this.model = model;
        this.templateInterpolator = templateInterpolator;
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSchemaIndex() {
        return templateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("rdfschema-index.xml")),
                model.createResource("http://miskinhill.com.au/rdfschema/1.0/"));
    }

}
