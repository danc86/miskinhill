package au.com.miskinhill.web;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.schema.unapi.Format;
import au.com.miskinhill.schema.unapi.Formats;

@Component
@Path("/unapi")
public class UnapiResource {
    
    private final Model model;
    private final RepresentationFactory representationFactory;
    private final Formats allFormats; // computed once for efficiency
    
    @Autowired
    public UnapiResource(Model model, RepresentationFactory representationFactory) {
        this.model = model;
        this.representationFactory = representationFactory;
        
        allFormats = new Formats();
        for (Representation r: representationFactory.getAll())
            allFormats.add(new Format(r));
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Formats unapi(@QueryParam("id") String id, @QueryParam("format") String format) {
        if (id != null) {
            Resource resource = model.createResource(id);
            if (!resource.listProperties().hasNext())
                throw new NotFoundException();
            if (format != null) {
                Representation representation = representationFactory.getRepresentationByFormat(format);
                if (representation == null)
                    throw new WebApplicationException(Status.NOT_ACCEPTABLE);
                if (!representation.canRepresent(resource))
                    throw new WebApplicationException(Status.NOT_ACCEPTABLE);
                Response redirect = Response.status(HttpServletResponse.SC_FOUND)
                        .location(URI.create(id + "." + format)).build();
                throw new WebApplicationException(redirect);
            }
            return Formats.forId(id, representationFactory.getRepresentationsForResource(resource));
        }
        return allFormats;
    }

}
