package au.com.miskinhill.web.feeds;

import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

@Component
@Path("/feeds/issues")
public class IssuesFeedResource {
	
	private final Model model;
	private final TemplateInterpolator templateInterpolator;
	
	@Autowired
	public IssuesFeedResource(Model model, TemplateInterpolator templateInterpolator) {
		this.model = model;
		this.templateInterpolator = templateInterpolator;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public String getIssues() {
	    return templateInterpolator.interpolate(
	            new InputStreamReader(this.getClass().getResourceAsStream("issues-feed.xml")),
	            model.createResource("http://miskinhill.com.au/journals/asees/")); // XXX dodgy
	}

}
