package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Rights extends AbstractElement<String> {
    
    protected Rights() {
    }
    
    public Rights(String value) {
        super(value);
    }
    
    public Rights(String value, String lang) {
        super(value, lang);
    }

}
