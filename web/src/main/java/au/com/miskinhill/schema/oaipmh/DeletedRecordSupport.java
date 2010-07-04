package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum DeletedRecordSupport {

    @XmlEnumValue("no")
    NO,
    @XmlEnumValue("persistent")
    PERSISTENT,
    @XmlEnumValue("transient")
    TRANSIENT;

}
