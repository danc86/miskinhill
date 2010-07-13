package au.com.miskinhill.web.oaipmh;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;

import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.ListSetsResponse;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;

@Component
public class ListSetsHandler implements VerbHandler<ListSetsResponse> {

    @Override
    public Verb getHandledVerb() {
        return Verb.LIST_SETS;
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
    public ListSetsResponse handle(Request request) throws ErrorResponseException {
        throw new ErrorResponseException(ErrorCode.NO_SET_HIERARCHY, "This repository does not support sets");
    }

}
