package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class SetDescription {

    @XmlAnyElement(lax = true)
    private Object any;
    
    protected SetDescription() {
    }
    
    public SetDescription(Object any) {
        this.any = any;
    }

    public Object getAny() {
        return any;
    }

}
