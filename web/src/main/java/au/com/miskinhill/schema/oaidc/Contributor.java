package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Contributor extends AbstractElement<String> {
    
    protected Contributor() {
    }
    
    public Contributor(String value) {
        super(value);
    }
    
    public Contributor(String value, String lang) {
        super(value, lang);
    }

}
