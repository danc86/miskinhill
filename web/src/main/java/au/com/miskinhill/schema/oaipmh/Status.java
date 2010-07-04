package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Status {

    @XmlEnumValue("deleted")
    DELETED;

}
