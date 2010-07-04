package au.com.miskinhill.schema.xmladapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public String marshal(LocalDate v) throws Exception {
        if (v == null) return null;
        return v.toString();
    }

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        if (v == null) return null;
        return new LocalDate(v);
    }

}
