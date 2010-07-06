package au.com.miskinhill.web.oaipmh;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.schema.oaipmh.DeletedRecordSupport;
import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.Granularity;
import au.com.miskinhill.schema.oaipmh.IdentifyResponse;
import au.com.miskinhill.schema.oaipmh.ListIdentifiersResponse;
import au.com.miskinhill.schema.oaipmh.ListMetadataFormatsResponse;
import au.com.miskinhill.schema.oaipmh.MetadataFormat;
import au.com.miskinhill.schema.oaipmh.OAIPMH;
import au.com.miskinhill.schema.oaipmh.RecordHeader;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Response;
import au.com.miskinhill.schema.oaipmh.Verb;
import au.com.miskinhill.web.rdf.TimestampDeterminer;

@Controller
@RequestMapping(value = "/oaipmh")
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
    
    @RequestMapping(params = "!verb")
    @ResponseBody
    public OAIPMH<?> noVerb() {
        Request request = new Request.Builder(REPOSITORY_BASE).build();
        return new OAIPMH<Response>(new DateTime(), request, Arrays.asList(
                new au.com.miskinhill.schema.oaipmh.Error(ErrorCode.BAD_VERB, "verb parameter missing")));
    }
    
    @RequestMapping
    @ResponseBody
    public OAIPMH<?> unrecognisedVerb(@RequestParam String verb) {
        Request request = new Request.Builder(REPOSITORY_BASE).build();
        return new OAIPMH<Response>(new DateTime(), request, Arrays.asList(
                new au.com.miskinhill.schema.oaipmh.Error(ErrorCode.BAD_VERB, "Unrecognised verb " + verb)));
    }
    
    @RequestMapping(params = "verb=Identify")
    @ResponseBody
    public OAIPMH<?> identify() {
        Request request = new Request.Builder(REPOSITORY_BASE).forVerb(Verb.IDENTIFY).build();
        return new OAIPMH<IdentifyResponse>(new DateTime(), request,
                new IdentifyResponse(REPOSITORY_NAME, REPOSITORY_BASE,
                        timestampDeterminer.getEarliestResourceTimestamp(),
                        ADMIN_EMAILS, DeletedRecordSupport.NO, Granularity.DATE_TIME, Collections.<String>emptyList()));
    }
    
    @RequestMapping(params = {"verb=ListMetadataFormats", "!identifier"})
    @ResponseBody
    public OAIPMH<?> listMetadataFormatsForAll() {
        Request request = new Request.Builder(REPOSITORY_BASE).forVerb(Verb.LIST_METADATA_FORMATS).build();
        return new OAIPMH<ListMetadataFormatsResponse>(new DateTime(), request,
                new ListMetadataFormatsResponse(Arrays.asList(
                    new MetadataFormat("oai_dc",
                            URI.create("http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
                            URI.create("http://www.openarchives.org/OAI/2.0/oai_dc/")))));
    }
    
    @RequestMapping(params = {"verb=ListMetadataFormats", "identifier"})
    @ResponseBody
    public OAIPMH<?> listMetadataFormatsForItem(@RequestParam String identifier) {
        Request request = new Request.Builder(REPOSITORY_BASE).forVerb(Verb.LIST_METADATA_FORMATS).forIdentifier(identifier).build();
        if (!existsInModel(model, identifier)) {
            return new OAIPMH<Response>(new DateTime(), request,
                    Arrays.asList(new au.com.miskinhill.schema.oaipmh.Error(ErrorCode.ID_DOES_NOT_EXIST,
                        "Identifier " + identifier + " is not known to this repository")));
        }
        return new OAIPMH<ListMetadataFormatsResponse>(new DateTime(), request,
                new ListMetadataFormatsResponse(Arrays.asList(
                        new MetadataFormat("oai_dc",
                                URI.create("http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
                                URI.create("http://www.openarchives.org/OAI/2.0/oai_dc/")))));
    }
    
    @RequestMapping(params = {"verb=ListIdentifiers", "!metadataPrefix"})
    @ResponseBody
    public OAIPMH<?> listIdentifiersWithoutMetadataPrefixError() {
        Request request = new Request.Builder(REPOSITORY_BASE).forVerb(Verb.LIST_IDENTIFIERS).build();
        return new OAIPMH<Response>(new DateTime(), request,
                Arrays.asList(new au.com.miskinhill.schema.oaipmh.Error(ErrorCode.BAD_ARGUMENT,
                        "metadataPrefix parameter missing")));
    }
    
    @RequestMapping(params = {"verb=ListIdentifiers", "metadataPrefix"})
    @ResponseBody
    public OAIPMH<?> listIdentifiers(@RequestParam String metadataPrefix) {
        Request request = new Request.Builder(REPOSITORY_BASE).forVerb(Verb.LIST_IDENTIFIERS).forMetadataPrefix(metadataPrefix).build();
        
        if (!metadataPrefix.equals("oai_dc")) {
            return new OAIPMH<Response>(new DateTime(), request,
                    Arrays.asList(new au.com.miskinhill.schema.oaipmh.Error(ErrorCode.CANNOT_DISSEMINATE_FORMAT,
                            "Metadata prefix " + metadataPrefix + " is not supported")));
        }
        
        List<RecordHeader> headers = new ArrayList<RecordHeader>();
        for (ResIterator it = model.listSubjectsWithProperty(RDF.type, MHS.IssueContent); it.hasNext(); ) {
            Resource resource = it.next();
            if (resource.getURI().startsWith("http://miskinhill.com.au/journals/")) {
                headers.add(new RecordHeader(resource.getURI(), timestampDeterminer.determineTimestamp(resource, null))); // XXX representation?
            }
        }
        
        return new OAIPMH<ListIdentifiersResponse>(new DateTime(), request, new ListIdentifiersResponse(headers));
    }
    
    private boolean existsInModel(Model model, String uri) {
        return model.createResource(uri).listProperties().hasNext();
    }

}
