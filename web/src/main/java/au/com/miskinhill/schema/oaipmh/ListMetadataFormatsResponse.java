package au.com.miskinhill.schema.oaipmh;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "metadataFormats"
})
@XmlRootElement(name = "ListMetadataFormats")
public class ListMetadataFormatsResponse extends Response {

    @XmlElement(required = true, name = "metadataFormat")
    private List<MetadataFormat> metadataFormats;
    
    protected ListMetadataFormatsResponse() {
        super(Verb.LIST_METADATA_FORMATS);
    }
    
    public ListMetadataFormatsResponse(List<MetadataFormat> metadataFormats) {
        super(Verb.LIST_METADATA_FORMATS);
        this.metadataFormats = metadataFormats;
    }
    
    public List<MetadataFormat> getMetadataFormats() {
        return metadataFormats;
    }

}
