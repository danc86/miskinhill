package au.com.miskinhill.schema.sitemaps;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "urlset")
@XmlType
public class Urlset {

    @XmlElement(name = "url", required = true)
    protected List<Url> urls = new ArrayList<Url>();

    public List<Url> getUrls() {
        return urls;
    }
    
    public void add(Url url) {
    	urls.add(url);
    }

}