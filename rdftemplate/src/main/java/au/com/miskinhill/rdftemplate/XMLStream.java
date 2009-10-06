package au.com.miskinhill.rdftemplate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

public class XMLStream implements Iterable<XMLEvent> {
    
    private final List<XMLEvent> events;
    
    public XMLStream(XMLEvent... events) {
        this.events = Arrays.asList(events);
    }
    
    public XMLStream(List<XMLEvent> events) {
        this.events = events;
    }
    
    @Override
    public Iterator<XMLEvent> iterator() {
        return events.iterator();
    }

}
