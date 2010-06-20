package au.com.miskinhill.web.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation;

// TODO something better than this (dc:modified, modtime of templates, ...?)
@Component
public class FixedTimestampDeterminer implements TimestampDeterminer {
    
    private static final Logger LOG = Logger.getLogger(FixedTimestampDeterminer.class.getName());
    
    private final DateTime timestamp;
    
    @Autowired
    public FixedTimestampDeterminer(@Qualifier("modelTimestamp") DateTime modelTimestamp) throws IOException {
        this.timestamp = maxInstant(getBuildTimestamp(), modelTimestamp);
        LOG.info("Using " + timestamp + " as fixed timestamp");
    }
    
    @Override
    public DateTime determineTimestamp(Resource resource, Representation representation) {
        return timestamp;
    }
    
    private DateTime getBuildTimestamp() throws IOException {
        Properties buildProperties = new Properties();
        InputStream stream = getClass().getResourceAsStream("/build.properties");
        if (stream == null)
            throw new IllegalStateException("build.properties does not exist");
        try {
            buildProperties.load(stream);
        } finally {
            stream.close();
        }
        String buildTimestamp = buildProperties.getProperty("Build-Timestamp");
        if (buildTimestamp == null)
            throw new IllegalStateException("Build-Timestamp not found in build.properties");
        return new DateTime(buildTimestamp);
    }
    
    private static <T extends ReadableInstant> T maxInstant(T left, T right) {
        if (left.compareTo(right) < 0) return right;
        return left;
    }

}
