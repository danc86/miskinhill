package au.com.miskinhill.schema.oaipmh;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.DateTime;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "identifier",
    "datestamp",
    "setSpec"
})
public class RecordHeader {

    @XmlElement(required = true)
    private String identifier;
    @XmlElement(required = true)
    private DateTime datestamp;
    @XmlElement
    private List<String> setSpec;
    @XmlAttribute
    private Status status;
    
    protected RecordHeader() {
    }
    
    public RecordHeader(String identifier, DateTime datestamp) {
        this.identifier = identifier;
        this.datestamp = datestamp;
        this.setSpec = Collections.emptyList();
    }
    
    public RecordHeader(String identifier, DateTime datestamp, List<String> setSpec) {
        this.identifier = identifier;
        this.datestamp = datestamp;
        this.setSpec = setSpec;
    }
    
    public RecordHeader(String identifier, DateTime datestamp, List<String> setSpec, Status status) {
        this.identifier = identifier;
        this.datestamp = datestamp;
        this.setSpec = setSpec;
        this.status = status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public DateTime getDatestamp() {
        return datestamp;
    }

    public List<String> getSetSpec() {
        return setSpec;
    }

    public Status getStatus() {
        return status;
    }

}
