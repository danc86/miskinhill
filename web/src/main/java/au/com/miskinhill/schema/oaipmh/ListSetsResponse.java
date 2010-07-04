package au.com.miskinhill.schema.oaipmh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "sets",
    "resumptionToken"
})
@XmlRootElement(name = "ListSets")
public class ListSetsResponse extends Response {

    @XmlElement(required = true, name = "set")
    private List<Set> sets;
    @XmlElement
    private ResumptionToken resumptionToken;
    
    protected ListSetsResponse() {
        super(Verb.LIST_SETS);
    }
    
    public ListSetsResponse(List<Set> sets) {
        super(Verb.LIST_SETS);
        this.sets = sets;
    }
    
    public List<Set> getSets() {
        return sets;
    }
    
    public ResumptionToken getResumptionToken() {
        return resumptionToken;
    }

}
