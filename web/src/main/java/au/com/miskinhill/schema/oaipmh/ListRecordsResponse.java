package au.com.miskinhill.schema.oaipmh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "records",
    "resumptionToken"
})
@XmlRootElement(name = "ListRecords")
public class ListRecordsResponse extends Response {

    @XmlElement(required = true, name = "record")
    private List<Record> records;
    @XmlElement
    private ResumptionToken resumptionToken;
    
    protected ListRecordsResponse() {
        super(Verb.LIST_RECORDS);
    }
    
    public ListRecordsResponse(List<Record> records) {
        super(Verb.LIST_RECORDS);
        this.records = records;
    }
    
    public ListRecordsResponse(List<Record> records, ResumptionToken resumptionToken) {
        super(Verb.LIST_RECORDS);
        this.records = records;
        this.resumptionToken = resumptionToken;
    }
    
    public List<Record> getRecords() {
        return records;
    }
    
    public ResumptionToken getResumptionToken() {
        return resumptionToken;
    }

}
