package au.com.miskinhill.web.oaipmh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation;
import au.com.miskinhill.rdf.Representation.ShownIn;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.rdf.XMLStreamRepresentation;
import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.ListMetadataFormatsResponse;
import au.com.miskinhill.schema.oaipmh.MetadataFormat;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Verb;

@Component
public class ListMetadataFormatsHandler implements VerbHandler<ListMetadataFormatsResponse> {
    
    private final Set<Argument> optionalArguments = Collections.unmodifiableSet(EnumSet.of(Argument.IDENTIFIER));
    private final OaipmhRepository repository;
    private final RepresentationFactory representationFactory;
    private final ListMetadataFormatsResponse allFormatsResponse;
    
    @Autowired
    public ListMetadataFormatsHandler(OaipmhRepository repository, RepresentationFactory representationFactory) {
        this.repository = repository;
        this.representationFactory = representationFactory;
        
        ArrayList<MetadataFormat> formats = new ArrayList<MetadataFormat>();
        for (Representation represenation: representationFactory.getAllRepresentationsShownIn(ShownIn.OAIPMH)) {
            XMLStreamRepresentation r = (XMLStreamRepresentation) represenation;
            formats.add(new MetadataFormat(r.getFormat(), r.getXSD(), r.getXMLNamespace()));
        }
        this.allFormatsResponse = new ListMetadataFormatsResponse(formats);
    }

    @Override
    public Verb getHandledVerb() {
        return Verb.LIST_METADATA_FORMATS;
    }

    @Override
    public Set<Argument> getRequiredArguments() {
        return Collections.emptySet();
    }

    @Override
    public Set<Argument> getOptionalArguments() {
        return optionalArguments;
    }

    @Override
    public ListMetadataFormatsResponse handle(Request request) throws ErrorResponseException {
        if (request.getIdentifier() == null)
            return allFormatsResponse;
        
        Resource resource = repository.getResourceInRepository(request.getIdentifier());
        if (resource == null)
            throw new ErrorResponseException(ErrorCode.ID_DOES_NOT_EXIST,
                    "Identifier " + request.getIdentifier() + " is not known to this repository");
        ArrayList<MetadataFormat> formats = new ArrayList<MetadataFormat>();
        for (Representation represenation: representationFactory.getRepresentationsForResource(resource, ShownIn.OAIPMH)) {
            XMLStreamRepresentation r = (XMLStreamRepresentation) represenation;
            formats.add(new MetadataFormat(r.getFormat(), r.getXSD(), r.getXMLNamespace()));
        }
        return new ListMetadataFormatsResponse(formats);
    }

}
