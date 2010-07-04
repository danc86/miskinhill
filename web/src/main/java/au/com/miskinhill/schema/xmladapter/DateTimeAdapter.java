package au.com.miskinhill.schema.xmladapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {
    
    private static final DateTimeFormatter FORMAT = ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed();
    
    @Override
    public String marshal(DateTime v) throws Exception {
        if (v == null) return null;
        return FORMAT.print(v);
    }
    
    @Override
    public DateTime unmarshal(String v) throws Exception {
        if (v == null) return null;
        return FORMAT.parseDateTime(v);
    }
    
}