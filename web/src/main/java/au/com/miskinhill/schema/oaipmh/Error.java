package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "code"
})
public class Error {

    @XmlAttribute(required = true)
    private ErrorCode code;
    @XmlValue
    private String message;
    
    protected Error() {
    }
    
    public Error(ErrorCode code) {
        this.code = code;
    }
    
    public Error(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public ErrorCode getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }

}
