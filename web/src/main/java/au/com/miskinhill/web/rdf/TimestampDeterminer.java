package au.com.miskinhill.web.rdf;

import com.hp.hpl.jena.rdf.model.Resource;
import org.joda.time.DateTime;

import au.com.miskinhill.rdf.Representation;

public interface TimestampDeterminer {
    
    DateTime determineTimestamp(Resource resource, Representation representation);
    
    DateTime getEarliestResourceTimestamp();
    
    DateTime getLatestResourceTimestamp();
    
    DateTime getBuildTimestamp();

}
