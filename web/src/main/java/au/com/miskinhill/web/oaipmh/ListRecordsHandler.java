package au.com.miskinhill.web.oaipmh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation.ShownIn;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.rdf.XMLStreamRepresentation;
import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.ListRecordsResponse;
import au.com.miskinhill.schema.oaipmh.Metadata;
import au.com.miskinhill.schema.oaipmh.Record;
import au.com.miskinhill.schema.oaipmh.RecordHeader;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Component
public class ListRecordsHandler implements VerbHandler<ListRecordsResponse> {
    
    private final Set<Argument> requiredArguments = Collections.unmodifiableSet(EnumSet.of(Argument.METADATA_PREFIX));
    private final Set<Argument> optionalArguments = Collections.unmodifiableSet(EnumSet.of(Argument.FROM, Argument.UNTIL, Argument.SET));
    private final OaipmhRepository repository;
    private final RepresentationFactory representationFactory;
    private final TimestampDeterminer timestampDeterminer;

    @Autowired
    public ListRecordsHandler(OaipmhRepository repository, RepresentationFactory representationFactory,
            TimestampDeterminer timestampDeterminer) {
        this.repository = repository;
        this.representationFactory = representationFactory;
        this.timestampDeterminer = timestampDeterminer;
    }

    @Override
    public Verb getHandledVerb() {
        return Verb.LIST_RECORDS;
    }

    @Override
    public Set<Argument> getRequiredArguments() {
        return requiredArguments;
    }

    @Override
    public Set<Argument> getOptionalArguments() {
        return optionalArguments;
    }

    @Override
    public ListRecordsResponse handle(Request request) throws ErrorResponseException {
        if (request.getSet() != null)
            throw new ErrorResponseException(ErrorCode.NO_SET_HIERARCHY, "This repository does not support sets");
        
        XMLStreamRepresentation representation = (XMLStreamRepresentation)
            representationFactory.getRepresentationByFormat(request.getMetadataPrefix());
        if (representation == null || !representation.isShownIn(ShownIn.OAIPMH))
            throw new ErrorResponseException(ErrorCode.CANNOT_DISSEMINATE_FORMAT,
                    "Metadata prefix " + request.getMetadataPrefix() + " is not supported");
        
        List<Record> records = new ArrayList<Record>();
        for (Iterator<Resource> it = repository.getAllResourcesInRepository(); it.hasNext(); ) {
            Resource resource = it.next();
            DateTime timestamp = timestampDeterminer.determineTimestamp(resource, representation);
            if (request.getFrom() != null && request.getFrom().isAfter(timestamp))
                continue;
            if (request.getUntil() != null && request.getUntil().isBefore(timestamp))
                continue;
            records.add(new Record(
                    new RecordHeader(repository.getIdentifierForResource(resource), timestamp),
                    new Metadata(representation.renderXMLStream(resource))));
        }
        if (records.isEmpty())
            throw new ErrorResponseException(ErrorCode.NO_RECORDS_MATCH, "Filter criteria yielded empty result set");
        
        return new ListRecordsResponse(records);
    }

}
