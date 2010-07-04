package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Source extends AbstractElement<String> {
    
    protected Source() {
    }
    
    public Source(String value) {
        super(value);
    }
    
    public Source(String value, String lang) {
        super(value, lang);
    }

}
