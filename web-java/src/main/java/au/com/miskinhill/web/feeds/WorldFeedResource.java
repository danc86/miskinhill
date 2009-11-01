package au.com.miskinhill.web.feeds;

import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/feeds/world")
public class WorldFeedResource {
	
	private final Model model;
	
	@Autowired
	public WorldFeedResource(Model model) {
		this.model = model;
	}
	
	@GET
	@Produces("application/rdf+xml")
	public String getWorldFeed() {
        StringWriter w = new StringWriter();
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        model.write(w, "RDF/XML-ABBREV");
        return w.toString();
	}

}
