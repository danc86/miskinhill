package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Publisher extends AbstractElement<String> {
    
    protected Publisher() {
    }
    
    public Publisher(String value) {
        super(value);
    }
    
    public Publisher(String value, String lang) {
        super(value, lang);
    }

}
