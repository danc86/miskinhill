package au.com.miskinhill.schema.oaipmh;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "setSpec",
    "name",
    "descriptions"
})
public class Set {

    @XmlElement(required = true)
    private String setSpec;
    @XmlElement(required = true, name = "setName")
    private String name;
    @XmlElement(name = "setDescription")
    private List<Description> descriptions;
    
    protected Set() {
    }
    
    public Set(String setSpec, String name) {
        this.setSpec = setSpec;
        this.name = name;
        this.descriptions = Collections.emptyList();
    }
    
    public Set(String setSpec, String name, List<Description> descriptions) {
        this.setSpec = setSpec;
        this.name = name;
        this.descriptions = descriptions;
    }

    public String getSetSpec() {
        return setSpec;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Description> getDescriptions() {
        return descriptions;
    }

}
