package au.com.miskinhill.schema.sitemaps;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.format.ISODateTimeFormat;

import org.joda.time.format.DateTimeFormatter;

import org.joda.time.DateTime;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Url {
    
    private static final class DateTimeAdapter extends XmlAdapter<String, DateTime> {
        private static final DateTimeFormatter FORMAT = ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed();
        @Override
        public String marshal(DateTime v) throws Exception {
            return FORMAT.print(v);
        }
        @Override
        public DateTime unmarshal(String v) throws Exception {
            return FORMAT.parseDateTime(v);
        }
    }

    @XmlElement(required = true)
    private String loc;
    @XmlElement
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private DateTime lastmod;
    @XmlElement
    private ChangeFreq changefreq;
    @XmlElement
    private BigDecimal priority;
    
    public Url() {
	}
    
    public Url(String loc) {
    	this.loc = loc;
    }
    
    public Url(String loc, DateTime lastmod) {
        this.loc = loc;
        this.lastmod = lastmod;
    }
    
    public Url(String loc, DateTime lastmod, ChangeFreq changefreq,
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

    public DateTime getLastmod() {
        return lastmod;
    }

    public void setLastmod(DateTime value) {
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
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("loc", loc)
                .append("lastmod", lastmod)
                .append("changefreq", changefreq)
                .append("priority", priority)
                .toString();
    }

}
