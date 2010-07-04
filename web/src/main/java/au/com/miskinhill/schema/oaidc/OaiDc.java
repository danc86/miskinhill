package au.com.miskinhill.schema.oaidc;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "dc")
public class OaiDc {

    @XmlElementRef
    private List<AbstractElement<?>> elements;
    
    protected OaiDc() {
    }
    
    public OaiDc(List<AbstractElement<?>> elements) {
        this.elements = elements;
    }
    
    public OaiDc(AbstractElement<?>... elements) {
        this.elements = Arrays.asList(elements);
    }
    
    public List<AbstractElement<?>> getElements() {
        return elements;
    }

}
