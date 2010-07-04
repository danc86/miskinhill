package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Type extends AbstractElement<String> {
    
    protected Type() {
    }
    
    public Type(String value) {
        super(value);
    }
    
    public Type(String value, String lang) {
        super(value, lang);
    }

}
