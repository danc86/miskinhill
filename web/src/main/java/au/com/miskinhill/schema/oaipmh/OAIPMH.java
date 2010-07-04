package au.com.miskinhill.schema.oaipmh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "responseDate",
    "request",
    "response",
    "errors"
})
@XmlRootElement(name = "OAI-PMH")
public class OAIPMH<T extends Response> {

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private DateTime responseDate;
    @XmlElement(required = true)
    private Request request;
    @XmlElementRef
    private T response;
    @XmlElement(name = "error")
    private List<Error> errors;
    
    protected OAIPMH() {
    }
    
    public OAIPMH(DateTime responseDate, Request request, T response) {
        if (request.getVerb() != response.getVerb())
            throw new IllegalArgumentException("Request verb " + request.getVerb() + 
                    " is inconsistent with response type " + response.getClass());
        this.responseDate = responseDate.toDateTime(DateTimeZone.UTC);
        this.request = request;
        this.response = response;
    }
    
    public OAIPMH(DateTime responseDate, Request request, List<Error> errors) {
        for (Error error: errors)
            if (!error.getCode().verbApplies(request.getVerb()))
                throw new IllegalArgumentException("Error code " + error.getCode() +
                        " is not applicable to verb " + request.getVerb());
        this.responseDate = responseDate.toDateTime(DateTimeZone.UTC);
        this.request = request;
        this.errors = errors;
    }

    public DateTime getResponseDate() {
        return responseDate;
    }

    public Request getRequest() {
        return request;
    }
    
    public T getResponse() {
        return response;
    }
    
    public List<Error> getErrors() {
        return errors;
    }

}
