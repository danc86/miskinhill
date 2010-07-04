package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Description extends AbstractElement<String> {
    
    protected Description() {
    }
    
    public Description(String value) {
        super(value);
    }
    
    public Description(String value, String lang) {
        super(value, lang);
    }

}
