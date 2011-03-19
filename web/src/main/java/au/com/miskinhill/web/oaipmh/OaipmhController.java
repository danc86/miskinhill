package au.com.miskinhill.web.oaipmh;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.miskinhill.schema.oaipmh.ErrorCode;
import au.com.miskinhill.schema.oaipmh.OAIPMH;
import au.com.miskinhill.schema.oaipmh.Request;
import au.com.miskinhill.schema.oaipmh.Request.Builder;
import au.com.miskinhill.schema.oaipmh.Response;
import au.com.miskinhill.schema.oaipmh.Verb;

@Controller
public class OaipmhController {
    
    static final URI REPOSITORY_BASE = URI.create("http://miskinhill.com.au/oaipmh");

    private final DateTimeFormatter dateTimeFormat = ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed();
    private final EnumMap<Verb, VerbHandler<?>> verbHandlers;
    
    @Autowired
    public OaipmhController(Set<VerbHandler<?>> verbHandlers) {
        this.verbHandlers = new EnumMap<Verb, VerbHandler<?>>(Verb.class);
        for (VerbHandler<?> verbHandler: verbHandlers)
            this.verbHandlers.put(verbHandler.getHandledVerb(), verbHandler);
        if (this.verbHandlers.size() != Verb.values().length)
            throw new IllegalStateException("Missing " + (Verb.values().length - this.verbHandlers.size()) +
                    " OAI-PMH verb handlers " +
            		"(found " + this.verbHandlers + ")");
    }
    
    @RequestMapping(value = "/oaipmh")
    @ResponseBody
    public OAIPMH<?> oaipmh(@RequestParam MultiValueMap<String, String> params) {
        Request.Builder requestBuilder = new Request.Builder(REPOSITORY_BASE);
        try {
            checkForDuplicateParams(params);
            Verb verb = extractVerb(params);
            requestBuilder.forVerb(verb);
            VerbHandler<?> handler = verbHandlers.get(verb);
            checkForResumptionToken(params);
            checkForRequiredParams(params, handler.getRequiredArguments());
            EnumSet<Argument> permittedArguments = EnumSet.of(Argument.VERB);
            permittedArguments.addAll(handler.getRequiredArguments());
            permittedArguments.addAll(handler.getOptionalArguments());
            checkForExtraneousParams(params, permittedArguments);
            populateRequestFromParams(params, requestBuilder);
            Request request = requestBuilder.build();
            Response response = handler.handle(request);
            return new OAIPMH<Response>(new DateTime(), request, response);
        } catch (ErrorResponseException e) {
            return new OAIPMH<Response>(new DateTime(), requestBuilder.build(), Arrays.asList(e.getError()));
        }
    }

    private void checkForDuplicateParams(MultiValueMap<String, String> params) throws ErrorResponseException {
        for (Entry<String, List<String>> param: params.entrySet()) {
            if (param.getValue().size() > 1)
                throw new ErrorResponseException(ErrorCode.BAD_ARGUMENT, param.getKey() + " parameter has multiple values");
        }
    }
    
    private Verb extractVerb(MultiValueMap<String, String> params) throws ErrorResponseException {
        String verb = params.getFirst("verb");
        if (verb == null)
            throw new ErrorResponseException(ErrorCode.BAD_VERB, "verb parameter missing");
        Verb verbEnum = Verb.forProtocolValue(verb);
        if (verbEnum == null)
            throw new ErrorResponseException(ErrorCode.BAD_VERB, "Unrecognised verb " + verb);
        return verbEnum;
    }
    
    private void checkForResumptionToken(MultiValueMap<String, String> params) throws ErrorResponseException {
        if (params.containsKey("resumptionToken"))
            throw new ErrorResponseException(ErrorCode.BAD_RESUMPTION_TOKEN, "Unexpected resumption token");
    }
    
    private void checkForRequiredParams(MultiValueMap<String, String> params, Set<Argument> required) throws ErrorResponseException {
        for (Argument argument: required)
            if (!params.containsKey(argument.getProtocolValue()))
                throw new ErrorResponseException(ErrorCode.BAD_ARGUMENT, argument.getProtocolValue() + " parameter missing");
    }

    private void checkForExtraneousParams(MultiValueMap<String, String> params, Set<Argument> permitted) throws ErrorResponseException {
        Set<String> permittedParams = new HashSet<String>();
        for (Argument argument: permitted) permittedParams.add(argument.getProtocolValue());
        for (String param: params.keySet())
            if (!permittedParams.contains(param))
                throw new ErrorResponseException(ErrorCode.BAD_ARGUMENT, "Unexpected parameter " + param);
    }

    private void populateRequestFromParams(MultiValueMap<String, String> params, Builder requestBuilder) throws ErrorResponseException {
        for (Argument argument: Argument.values()) {
            String paramValue = params.getFirst(argument.getProtocolValue());
            if (paramValue == null) continue;
            switch (argument) {
                case VERB:
                    break; // already handled
                case IDENTIFIER:
                    requestBuilder.forIdentifier(paramValue);
                    break;
                case METADATA_PREFIX:
                    requestBuilder.forMetadataPrefix(paramValue);
                    break;
                case FROM:
                    requestBuilder.from(parseDateTimeParameter(argument.getProtocolValue(), paramValue));
                    break;
                case UNTIL:
                    requestBuilder.until(parseDateTimeParameter(argument.getProtocolValue(), paramValue));
                    break;
                case SET:
                    requestBuilder.forSet(paramValue);
                    break;
            }
        }
    }
    
    private DateTime parseDateTimeParameter(String paramName, String paramValue) throws ErrorResponseException {
        DateTime result;
        try {
            result = dateTimeFormat.parseDateTime(paramValue);
        } catch (IllegalArgumentException e) {
            throw new ErrorResponseException(ErrorCode.BAD_ARGUMENT, paramName + " parameter could not be parsed");
        }
        if (!result.getZone().equals(DateTimeZone.UTC))
            throw new ErrorResponseException(ErrorCode.BAD_ARGUMENT, paramName + " parameter was not in UTC");
        return result;
    }

}
