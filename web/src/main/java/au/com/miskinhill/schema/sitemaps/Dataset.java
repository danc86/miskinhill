package au.com.miskinhill.schema.sitemaps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Dataset {

    @XmlElement(namespace = "http://sw.deri.org/2007/07/sitemapextension/scschema.xsd")
    private String dataDumpLocation;
    
    public Dataset() {
    }
    
    public Dataset(String dataDumpLocation) {
    	this.dataDumpLocation = dataDumpLocation;
    }

    public String getDataDumpLocation() {
        return dataDumpLocation;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("dataDumpLocation", dataDumpLocation)
                .toString();
    }

}
