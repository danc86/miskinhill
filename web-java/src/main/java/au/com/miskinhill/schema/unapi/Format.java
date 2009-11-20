package au.com.miskinhill.schema.unapi;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import au.com.miskinhill.rdf.Representation;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Format {
    
    private static final class MediaTypeAdapter extends XmlAdapter<String, MediaType> {
        @Override
        public String marshal(MediaType v) throws Exception {
            return v.toString();
        }
        @Override
        public MediaType unmarshal(String v) throws Exception {
            return MediaType.valueOf(v);
        }
    }

    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(MediaTypeAdapter.class)
    private MediaType type;
    @XmlAttribute
    private String docs;

    /** Required by JAXB, do not use */
    public Format() {
    }
    
    public Format(String name, MediaType type) {
        this.name = name;
        this.type = type;
    }
    
    public Format(String name, MediaType type, String docs) {
        this.name = name;
        this.type = type;
        this.docs = docs;
    }
    
    public Format(Representation representation) {
        this.name = representation.getLabel();
        this.type = representation.getContentType();
        this.docs = representation.getDocs();
    }
    
    public String getName() {
        return name;
    }
    
    public MediaType getType() {
        return type;
    }
    
    public String getDocs() {
        return docs;
    }

}
