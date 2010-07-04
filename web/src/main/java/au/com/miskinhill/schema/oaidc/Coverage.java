package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Coverage extends AbstractElement<String> {
    
    protected Coverage() {
    }
    
    public Coverage(String value) {
        super(value);
    }
    
    public Coverage(String value, String lang) {
        super(value, lang);
    }

}
