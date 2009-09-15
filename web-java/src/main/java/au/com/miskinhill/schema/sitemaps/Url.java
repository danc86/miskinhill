package au.com.miskinhill.schema.sitemaps;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Url {

    @XmlElement(required = true)
    private String loc;
    @XmlElement
    private String lastmod;
    @XmlElement
    private ChangeFreq changefreq;
    @XmlElement
    private BigDecimal priority;
    
    public Url() {
	}
    
    public Url(String loc) {
    	this.loc = loc;
    }
    
    public Url(String loc, String lastmod, ChangeFreq changefreq,
    		BigDecimal priority) {
    	this.loc = loc;
    	this.lastmod = lastmod;
    	this.changefreq = changefreq;
    	this.priority = priority;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String value) {
        this.loc = value;
    }

    public String getLastmod() {
        return lastmod;
    }

	/**
	 * Should be given in <a href="http://www.w3.org/TR/NOTE-datetime">W3C
	 * datetime format</a>.
	 */
    public void setLastmod(String value) {
        this.lastmod = value;
    }

    public ChangeFreq getChangefreq() {
        return changefreq;
    }

    public void setChangefreq(ChangeFreq value) {
        this.changefreq = value;
    }

    public BigDecimal getPriority() {
        return priority;
    }

    public void setPriority(BigDecimal value) {
    	if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
    		throw new IllegalArgumentException("Priority must be in [0, 1]");
    	}
        this.priority = value;
    }

}
