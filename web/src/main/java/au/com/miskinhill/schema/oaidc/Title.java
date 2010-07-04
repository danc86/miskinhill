package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Title extends AbstractElement<String> {
    
    protected Title() {
    }
    
    public Title(String value) {
        super(value);
    }
    
    public Title(String value, String lang) {
        super(value, lang);
    }

}
