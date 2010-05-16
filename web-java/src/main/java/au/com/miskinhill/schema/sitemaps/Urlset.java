package au.com.miskinhill.schema.sitemaps;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "urlset")
@XmlType
public class Urlset {

    @XmlElement(name = "url", required = true)
    private List<Url> urls = new ArrayList<Url>();
    @XmlElement(name = "dataset", namespace = "http://sw.deri.org/2007/07/sitemapextension/scschema.xsd")
    private List<Dataset> datasets = new ArrayList<Dataset>();

    public List<Url> getUrls() {
        return urls;
    }
    
    public List<Dataset> getDatasets() {
        return datasets;
    }
    
    public void add(Url url) {
    	urls.add(url);
    }
    
    public void add(Dataset dataset) {
        datasets.add(dataset);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(urls).toString();
    }
    
    public boolean containsLoc(String loc) {
        for (Url url: urls) {
            if (url.getLoc().equals(loc))
                return true;
        }
        return false;
    }

}
