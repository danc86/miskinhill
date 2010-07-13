package au.com.miskinhill.web.oaipmh;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation.ShownIn;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.rdf.XMLStreamRepresentation;
import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.GetRecordResponse;
import au.com.miskinhill.schema.oaipmh.Metadata;
import au.com.miskinhill.schema.oaipmh.Record;
import au.com.miskinhill.schema.oaipmh.RecordHeader;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Component
public class GetRecordHandler implements VerbHandler<GetRecordResponse> {
    
    private final Set<Argument> requiredArguments = Collections.unmodifiableSet(EnumSet.of(Argument.IDENTIFIER, Argument.METADATA_PREFIX));
    private final OaipmhRepository repository;
    private final RepresentationFactory representationFactory;
    private final TimestampDeterminer timestampDeterminer;

    @Autowired
    public GetRecordHandler(OaipmhRepository repository, RepresentationFactory representationFactory,
            TimestampDeterminer timestampDeterminer) {
        this.repository = repository;
        this.representationFactory = representationFactory;
        this.timestampDeterminer = timestampDeterminer;
    }

    @Override
    public Verb getHandledVerb() {
        return Verb.GET_RECORD;
    }

    @Override
    public Set<Argument> getRequiredArguments() {
        return requiredArguments;
    }

    @Override
    public Set<Argument> getOptionalArguments() {
        return Collections.emptySet();
    }

    @Override
    public GetRecordResponse handle(Request request) throws ErrorResponseException {
        XMLStreamRepresentation representation = (XMLStreamRepresentation)
            representationFactory.getRepresentationByFormat(request.getMetadataPrefix());
        if (representation == null || !representation.isShownIn(ShownIn.OAIPMH))
            throw new ErrorResponseException(ErrorCode.CANNOT_DISSEMINATE_FORMAT,
                    "Metadata prefix " + request.getMetadataPrefix() + " is not supported");
        Resource resource = repository.getResourceInRepository(request.getIdentifier());
        if (resource == null)
            throw new ErrorResponseException(ErrorCode.ID_DOES_NOT_EXIST,
                    "Identifier " + request.getIdentifier() + " is not known to this repository");
        RecordHeader header = new RecordHeader(repository.getIdentifierForResource(resource),
                timestampDeterminer.determineTimestamp(resource, representation));
        return new GetRecordResponse(new Record(header, new Metadata(representation.renderXMLStream(resource))));
    }

}
