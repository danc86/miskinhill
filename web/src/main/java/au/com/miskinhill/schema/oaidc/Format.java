package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Format extends AbstractElement<String> {
    
    protected Format() {
    }
    
    public Format(String value) {
        super(value);
    }
    
    public Format(String value, String lang) {
        super(value, lang);
    }

}
