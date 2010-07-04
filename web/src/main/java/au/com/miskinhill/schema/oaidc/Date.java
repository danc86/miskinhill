package au.com.miskinhill.schema.oaidc;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import au.com.miskinhill.schema.xmladapter.DateTimeAdapter;

@XmlRootElement(namespace = "http://purl.org/dc/elements/1.1/")
public class Date extends AbstractElement<DateTime> {
    
    protected Date() {
    }
    
    public Date(DateTime value) {
        // OAI-PMH 2.0 spec requires UTC: http://www.openarchives.org/OAI/openarchivesprotocol.html#Dates
        super(value.toDateTime(DateTimeZone.UTC));
    }
    
    @Override
    protected XmlAdapter<String, DateTime> getTypeAdapter() {
        return new DateTimeAdapter();
    }
    
}
