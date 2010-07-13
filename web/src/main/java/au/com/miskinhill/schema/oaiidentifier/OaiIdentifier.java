package au.com.miskinhill.schema.oaiidentifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "oai-identifier")
@XmlType(propOrder = {
    "scheme",
    "repositoryIdentifier",
    "delimiter",
    "sampleIdentifier"
})
public class OaiIdentifier {

    @XmlElement(required = true)
    private String scheme;
    @XmlElement(required = true)
    private String repositoryIdentifier;
    @XmlElement(required = true)
    private String delimiter;
    @XmlElement(required = true)
    private String sampleIdentifier;
    
    protected OaiIdentifier() {
    }
    
    public OaiIdentifier(String scheme, String repositoryIdentifier, String delimiter, String sampleIdentifier) {
        this.scheme = scheme;
        this.repositoryIdentifier = repositoryIdentifier;
        this.delimiter = delimiter;
        this.sampleIdentifier = sampleIdentifier;
    }

    public String getScheme() {
        return scheme;
    }

    public String getRepositoryIdentifier() {
        return repositoryIdentifier;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getSampleIdentifier() {
        return sampleIdentifier;
    }

}
