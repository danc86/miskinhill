package au.com.miskinhill.schema.oaipmh;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "repositoryName",
    "baseURL",
    "protocolVersion",
    "adminEmail",
    "earliestDatestamp",
    "deletedRecord",
    "granularity",
    "compression",
    "description"
})
@XmlRootElement(name = "Identify")
public class IdentifyResponse extends Response {

    @XmlElement(required = true)
    private String repositoryName;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    private String baseURL;
    @XmlElement(required = true)
    private String protocolVersion = "2.0";
    @XmlElement(required = true)
    private List<String> adminEmail;
    @XmlElement(required = true)
    private DateTime earliestDatestamp;
    @XmlElement(required = true)
    private DeletedRecordSupport deletedRecord;
    @XmlElement(required = true)
    private Granularity granularity;
    @XmlElement
    private List<String> compression;
    @XmlElement
    private List<Description> description;
    
    protected IdentifyResponse() {
        super(Verb.IDENTIFY);
    }
    
    public IdentifyResponse(String repositoryName, URI baseURL, DateTime earliestDatestamp, List<String> adminEmail,
            DeletedRecordSupport deletedRecord, Granularity granularity, List<String> compression, List<Description> descriptions) {
        super(Verb.IDENTIFY);
        this.repositoryName = repositoryName;
        this.baseURL = baseURL.toString();
        this.earliestDatestamp = earliestDatestamp.toDateTime(DateTimeZone.UTC);
        this.adminEmail = adminEmail;
        this.deletedRecord = deletedRecord;
        this.granularity = granularity;
        this.compression = compression;
        this.description = descriptions;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public List<String> getAdminEmail() {
        return adminEmail;
    }

    public DateTime getEarliestDatestamp() {
        return earliestDatestamp;
    }

    public DeletedRecordSupport getDeletedRecord() {
        return deletedRecord;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    public List<String> getCompression() {
        return compression;
    }

    public List<Description> getDescription() {
        return description;
    }

}
