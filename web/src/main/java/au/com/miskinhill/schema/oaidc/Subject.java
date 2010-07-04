package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Subject extends AbstractElement<String> {
    
    protected Subject() {
    }
    
    public Subject(String value) {
        super(value);
    }
    
    public Subject(String value, String lang) {
        super(value, lang);
    }

}
