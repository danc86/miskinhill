package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "GetRecord")
public class GetRecordResponse extends Response {

    @XmlElement(required = true)
    private Record record;
    
    protected GetRecordResponse() {
        super(Verb.GET_RECORD);
    }
    
    public GetRecordResponse(Record record) {
        super(Verb.GET_RECORD);
        this.record = record;
    }

    public Record getRecord() {
        return record;
    }

}
