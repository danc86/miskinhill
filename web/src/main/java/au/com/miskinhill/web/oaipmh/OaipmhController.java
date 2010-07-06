package au.com.miskinhill.web.oaipmh;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.miskinhill.schema.oaipmh.DeletedRecordSupport;
import au.com.miskinhill.schema.oaipmh.Granularity;
import au.com.miskinhill.schema.oaipmh.IdentifyResponse;
import au.com.miskinhill.schema.oaipmh.OAIPMH;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Controller
public class OaipmhController {
    
    private static final URI REPOSITORY_BASE = URI.create("http://miskinhill.com.au/oaipmh");
    private static final String REPOSITORY_NAME = "Miskin Hill Journal Articles Repository";
    private static final List<String> ADMIN_EMAILS = Arrays.asList("info@miskinhill.com.au");
    
    private final Model model;
    private final TimestampDeterminer timestampDeterminer;
    
    @Autowired
    public OaipmhController(Model model, TimestampDeterminer timestampDeterminer) {
        this.model = model;
        this.timestampDeterminer = timestampDeterminer;
    }
    
    @RequestMapping(value = "/oaipmh", method = RequestMethod.GET, params = "verb=Identify")
    @ResponseBody
    public OAIPMH<IdentifyResponse> identify() {
        return new OAIPMH<IdentifyResponse>(new DateTime(), new Request(REPOSITORY_BASE, Verb.IDENTIFY),
                new IdentifyResponse(REPOSITORY_NAME, REPOSITORY_BASE,
                        timestampDeterminer.getEarliestResourceTimestamp(),
                        ADMIN_EMAILS, DeletedRecordSupport.NO, Granularity.DATE_TIME, Collections.<String>emptyList()));
    }

}
