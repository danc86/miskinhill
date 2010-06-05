package au.com.miskinhill.web.feeds;

import java.util.Comparator;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.joda.time.DateTime;

public class AtomEntry {
    
    public static final Comparator<AtomEntry> PUBLISHED_COMPARATOR = new Comparator<AtomEntry>() {
        @Override
        public int compare(AtomEntry left, AtomEntry right) {
            return left.published.compareTo(right.published);
        }
    };
    public static final Comparator<AtomEntry> UPDATED_COMPARATOR = new Comparator<AtomEntry>() {
        @Override
        public int compare(AtomEntry left, AtomEntry right) {
            return left.updated.compareTo(right.updated);
        }
    };
    
    private final List<XMLEvent> events;
    private final DateTime published;
    private final DateTime updated;
    
    public AtomEntry(List<XMLEvent> events, DateTime published, DateTime updated) {
        this.events = events;
        this.published = published;
        this.updated = updated;
    }
    
    public List<XMLEvent> getEvents() {
        return events;
    }
    
    public DateTime getPublished() {
        return published;
    }
    
    public DateTime getUpdated() {
        return updated;
    }

}
