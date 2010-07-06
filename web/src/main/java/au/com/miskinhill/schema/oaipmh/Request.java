package au.com.miskinhill.schema.oaipmh;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.joda.time.DateTime;

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
    
    public static final class Builder {
        private final Request request = new Request();
        
        public Builder(URI baseUrl) {
            request.baseUrl = baseUrl.toString();
        }
        
        public Builder forVerb(Verb verb) {
            request.verb = verb;
            return this;
        }
        
        public Builder forIdentifier(String identifier) {
            request.identifier = identifier;
            return this;
        }
        
        public Builder forMetadataPrefix(String prefix) {
            request.metadataPrefix = prefix;
            return this;
        }
        
        public Builder from(DateTime from) {
            request.from = from;
            return this;
        }
        
        public Builder until(DateTime until) {
            request.until = until;
            return this;
        }
        
        public Builder forSet(String set) {
            request.set = set;
            return this;
        }
        
        public Request build() {
            return request;
        }
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
