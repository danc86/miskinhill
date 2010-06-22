package au.com.miskinhill.web.feeds;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WorldFeedController {
	
	private static final byte[] XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n".getBytes();
	
    private final Model bareModel;
	
	@Autowired
	public WorldFeedController(Model bareModel) {
		this.bareModel = bareModel;
	}
	
	@RequestMapping(value = "/feeds/world", method = RequestMethod.GET)
	public void getWorldFeed(HttpServletResponse response) throws IOException {
	    response.setContentType("application/rdf+xml");
	    OutputStream stream = response.getOutputStream();
        stream.write(XML_PREAMBLE);
        bareModel.write(stream, "RDF/XML-ABBREV");
        stream.flush();
	}

}
