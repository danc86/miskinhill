package au.com.miskinhill.web.oaipmh;

import java.util.Set;

import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Response;
import au.com.miskinhill.schema.oaipmh.Verb;

public interface VerbHandler<T extends Response> {
    
    Verb getHandledVerb();
    
    Set<Argument> getRequiredArguments();
    
    Set<Argument> getOptionalArguments();
    
    T handle(Request request) throws ErrorResponseException;

}
