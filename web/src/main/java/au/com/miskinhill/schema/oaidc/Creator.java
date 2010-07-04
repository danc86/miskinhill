package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Creator extends AbstractElement<String> {
    
    protected Creator() {
    }
    
    public Creator(String value) {
        super(value);
    }
    
    public Creator(String value, String lang) {
        super(value, lang);
    }

}
