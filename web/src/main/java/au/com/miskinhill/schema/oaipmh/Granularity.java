package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum Granularity {

    @XmlEnumValue("YYYY-MM-DD")
    DATE,
    @XmlEnumValue("YYYY-MM-DDThh:mm:ssZ")
    DATE_TIME;

}
