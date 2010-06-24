package au.com.miskinhill.web.feeds;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import au.com.miskinhill.web.rdf.FixedTimestampDeterminer;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Controller
public class WorldFeedController {
	
	private static final byte[] XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n".getBytes();
	
    private final Model bareModel;
    private final TimestampDeterminer timestampDeterminer;
	
	@Autowired
	public WorldFeedController(Model bareModel, TimestampDeterminer timestampDeterminer) {
		this.bareModel = bareModel;
		this.timestampDeterminer = timestampDeterminer;
	}
	
	@RequestMapping(value = {"/feeds/world", "/feeds/world.xml"}, method = RequestMethod.GET)
	public void getWorldFeed(WebRequest request, HttpServletResponse response) throws IOException {
        DateTime feedTimestamp = FixedTimestampDeterminer.maxInstant(timestampDeterminer.getLatestResourceTimestamp(),
                timestampDeterminer.getBuildTimestamp());
        if (request.checkNotModified(feedTimestamp.getMillis()))
	        return;
	    response.setContentType("application/rdf+xml");
	    OutputStream stream = response.getOutputStream();
        stream.write(XML_PREAMBLE);
        bareModel.write(stream, "RDF/XML-ABBREV");
        stream.flush();
	}

}
