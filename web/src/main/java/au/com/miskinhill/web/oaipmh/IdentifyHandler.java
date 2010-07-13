package au.com.miskinhill.web.oaipmh;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.schema.oaipmh.DeletedRecordSupport;
import au.com.miskinhill.schema.oaipmh.Granularity;
import au.com.miskinhill.schema.oaipmh.IdentifyResponse;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Component
public class IdentifyHandler implements VerbHandler<IdentifyResponse> {
    
    private final String repositoryName = "Miskin Hill Journal Articles Repository";
    private final List<String> adminEmails = Arrays.asList("info@miskinhill.com.au");
    private final TimestampDeterminer timestampDeterminer;
    
    @Autowired
    public IdentifyHandler(TimestampDeterminer timestampDeterminer) {
        this.timestampDeterminer = timestampDeterminer;
    }
    
    @Override
    public Verb getHandledVerb() {
        return Verb.IDENTIFY;
    }
    
    @Override
    public Set<Argument> getRequiredArguments() {
        return Collections.emptySet();
    }
    
    @Override
    public Set<Argument> getOptionalArguments() {
        return Collections.emptySet();
    }
    
    @Override
    public IdentifyResponse handle(Request request) {
        return new IdentifyResponse(repositoryName, OaipmhController.REPOSITORY_BASE,
                timestampDeterminer.getEarliestResourceTimestamp(),
                adminEmails, DeletedRecordSupport.NO, Granularity.DATE_TIME, Collections.<String>emptyList());
    }

}
