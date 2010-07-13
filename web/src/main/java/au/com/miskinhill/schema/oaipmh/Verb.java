package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Verb {

    @XmlEnumValue("Identify")
    IDENTIFY("Identify"),
    @XmlEnumValue("ListMetadataFormats")
    LIST_METADATA_FORMATS("ListMetadataFormats"),
    @XmlEnumValue("ListSets")
    LIST_SETS("ListSets"),
    @XmlEnumValue("GetRecord")
    GET_RECORD("GetRecord"),
    @XmlEnumValue("ListIdentifiers")
    LIST_IDENTIFIERS("ListIdentifiers"),
    @XmlEnumValue("ListRecords")
    LIST_RECORDS("ListRecords");
    
    private final String protocolValue;
    
    private Verb(String protocolValue) {
        this.protocolValue = protocolValue;
    }
    
    public static Verb forProtocolValue(String protocolValue) {
        for (Verb verb: values())
            if (verb.protocolValue.equals(protocolValue))
                return verb;
        return null;
    }

}
