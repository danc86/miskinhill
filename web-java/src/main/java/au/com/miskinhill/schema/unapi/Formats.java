package au.com.miskinhill.schema.unapi;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import au.com.miskinhill.rdf.Representation;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "formats")
@XmlType
public class Formats {
    
    @XmlAttribute
    private String id;
    @XmlElement(name = "format", required = true)
    private List<Format> formats = new ArrayList<Format>();
    
    public static Formats forId(String id) {
        Formats formats = new Formats();
        formats.id = id;
        return formats;
    }
    
    public static Formats forId(String id, Iterable<Representation> representations) {
        Formats formats = new Formats();
        formats.id = id;
        for (Representation r: representations)
            formats.add(new Format(r));
        return formats;
    }
    
    public Formats() {
    }
    
    public String getId() {
        return id;
    }
    
    public List<Format> getFormats() {
        return formats;
    }
    
    public void add(Format format) {
        this.formats.add(format);
    }

}
