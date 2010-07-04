package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Language extends AbstractElement<String> {
    
    protected Language() {
    }
    
    public Language(String value) {
        super(value);
    }
    
}
