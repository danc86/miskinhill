package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Relation extends AbstractElement<String> {
    
    protected Relation() {
    }
    
    public Relation(String value) {
        super(value);
    }
    
    public Relation(String value, String lang) {
        super(value, lang);
    }

}
