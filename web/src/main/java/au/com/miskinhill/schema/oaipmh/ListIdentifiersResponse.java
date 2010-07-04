package au.com.miskinhill.schema.oaipmh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "headers",
    "resumptionToken"
})
@XmlRootElement(name = "ListIdentifiers")
public class ListIdentifiersResponse extends Response {

    @XmlElement(required = true, name = "header")
    private List<RecordHeader> headers;
    @XmlElement
    private ResumptionToken resumptionToken;
    
    protected ListIdentifiersResponse() {
        super(Verb.LIST_IDENTIFIERS);
    }
    
    public ListIdentifiersResponse(List<RecordHeader> headers) {
        super(Verb.LIST_IDENTIFIERS);
        this.headers = headers;
    }
    
    public ListIdentifiersResponse(List<RecordHeader> headers, ResumptionToken resumptionToken) {
        super(Verb.LIST_IDENTIFIERS);
        this.headers = headers;
        this.resumptionToken = resumptionToken;
    }

    public List<RecordHeader> getHeaders() {
        return headers;
    }

    public ResumptionToken getResumptionToken() {
        return resumptionToken;
    }

}
