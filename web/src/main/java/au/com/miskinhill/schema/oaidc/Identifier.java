package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Identifier extends AbstractElement<String> {
    
    protected Identifier() {
    }
    
    public Identifier(String value) {
        super(value);
    }
    
    public Identifier(String value, String lang) {
        super(value, lang);
    }

}
