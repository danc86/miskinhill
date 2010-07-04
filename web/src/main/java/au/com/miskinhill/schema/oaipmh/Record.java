package au.com.miskinhill.schema.oaipmh;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "header",
    "metadata",
    "about"
})
public class Record {

    @XmlElement(required = true)
    private RecordHeader header;
    @XmlElement
    private Metadata metadata;
    @XmlElement
    private List<About> about;
    
    protected Record() {
    }
    
    public Record(RecordHeader header, Metadata metadata) {
        this.header = header;
        this.metadata = metadata;
        this.about = Collections.emptyList();
    }
    
    public Record(RecordHeader header, Metadata metadata, List<About> about) {
        this.header = header;
        this.metadata = metadata;
        this.about = about;
    }

    public RecordHeader getHeader() {
        return header;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<About> getAbout() {
        return about;
    }

}
