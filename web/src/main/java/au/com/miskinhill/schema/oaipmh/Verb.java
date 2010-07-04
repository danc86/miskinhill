package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Verb {

    @XmlEnumValue("Identify")
    IDENTIFY,
    @XmlEnumValue("ListMetadataFormats")
    LIST_METADATA_FORMATS,
    @XmlEnumValue("ListSets")
    LIST_SETS,
    @XmlEnumValue("GetRecord")
    GET_RECORD,
    @XmlEnumValue("ListIdentifiers")
    LIST_IDENTIFIERS,
    @XmlEnumValue("ListRecords")
    LIST_RECORDS;

}
