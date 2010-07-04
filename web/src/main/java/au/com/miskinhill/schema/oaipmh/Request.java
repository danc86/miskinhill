package au.com.miskinhill.schema.oaipmh;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Request {

    @XmlValue
    @XmlSchemaType(name = "anyURI")
    private String baseUrl;
    @XmlAttribute
    private Verb verb;
    @XmlAttribute
    private String identifier;
    @XmlAttribute
    private String metadataPrefix;
    @XmlAttribute
    private DateTime from;
    @XmlAttribute
    private DateTime until;
    @XmlAttribute
    private String set;
    @XmlAttribute
    private String resumptionToken;
    
    protected Request() {
    }
    
    public Request(URI baseUrl, Verb verb) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
    }
    
    public Request(URI baseUrl, Verb verb, String identifier) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.identifier = identifier;
    }
    
    public Request(URI baseUrl, Verb verb, String identifier, String metadataPrefix) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.identifier = identifier;
        this.metadataPrefix = metadataPrefix;
    }
    
    public Request(URI baseUrl, Verb verb, DateTime from, String metadataPrefix) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.from = from.toDateTime(DateTimeZone.UTC);
        this.metadataPrefix = metadataPrefix;
    }
    
    public Request(URI baseUrl, Verb verb, DateTime from, String metadataPrefix, String set) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.from = from.toDateTime(DateTimeZone.UTC);
        this.metadataPrefix = metadataPrefix;
        this.set = set;
    }
    
    public Request(URI baseUrl, Verb verb, DateTime from, DateTime until, String metadataPrefix) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.from = from.toDateTime(DateTimeZone.UTC);
        this.until = until.toDateTime(DateTimeZone.UTC);
        this.metadataPrefix = metadataPrefix;
    }
    
    public Request(URI baseUrl, Verb verb, DateTime from, DateTime until, String metadataPrefix, String set) {
        this.baseUrl = baseUrl.toString();
        this.verb = verb;
        this.from = from.toDateTime(DateTimeZone.UTC);
        this.until = until.toDateTime(DateTimeZone.UTC);
        this.metadataPrefix = metadataPrefix;
        this.set = set;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Verb getVerb() {
        return verb;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getUntil() {
        return until;
    }

    public String getSet() {
        return set;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

}
