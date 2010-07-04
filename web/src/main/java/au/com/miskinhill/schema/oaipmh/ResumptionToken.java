package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.joda.time.DateTime;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "value"
})
public class ResumptionToken {

    @XmlValue
    private String value;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    private DateTime expirationDate;
    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    private Integer completeListSize;
    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    private Integer cursor;
    
    protected ResumptionToken() {
    }
    
    public ResumptionToken(String value, DateTime expirationDate, int completeListSize, int cursor) {
        this.value = value;
        this.expirationDate = expirationDate;
        this.completeListSize = completeListSize;
        this.cursor = cursor;
    }

    public String getValue() {
        return value;
    }
    
    public DateTime getExpirationDate() {
        return expirationDate;
    }
    
    public Integer getCompleteListSize() {
        return completeListSize;
    }
    
    public Integer getCursor() {
        return cursor;
    }

}
