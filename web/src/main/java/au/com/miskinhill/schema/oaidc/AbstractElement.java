package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractElement<ValueType> {

    // @XmlValue would be here except JAXB is buggy :-(
    private ValueType value;
    
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    private String lang;
    
    protected AbstractElement() {
    }
    
    public AbstractElement(ValueType value) {
        this.value = value;
    }
    
    public AbstractElement(ValueType value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    public ValueType getValue() {
        return value;
    }
    
    @SuppressWarnings("unused")
    @XmlValue
    private String getValueHack() throws Exception {
        if (getTypeAdapter() != null)
            return getTypeAdapter().marshal(value);
        return value.toString();
    }
    
    protected XmlAdapter<String, ValueType> getTypeAdapter() {
        return null;
    }

    public String getLang() {
        return lang;
    }

}
