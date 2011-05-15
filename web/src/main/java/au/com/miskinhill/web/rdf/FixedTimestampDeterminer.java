package au.com.miskinhill.web.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.Representation;

// TODO something better than this (dc:modified, modtime of templates, ...?)
@Component
public class FixedTimestampDeterminer implements TimestampDeterminer {
    
    private static final Logger LOG = Logger.getLogger(FixedTimestampDeterminer.class.getName());
    
    private final DateTime buildTimestamp;
    private final DateTime modelTimestamp;
    private final DateTime timestamp;
    
    @Autowired
    public FixedTimestampDeterminer(@Qualifier("modelTimestamp") DateTime modelTimestamp) throws IOException {
        this.buildTimestamp = loadBuildTimestamp();
        this.modelTimestamp = modelTimestamp;
        this.timestamp = maxInstant(buildTimestamp, modelTimestamp);
        LOG.info("Using " + timestamp + " as fixed timestamp");
    }
    
    @Override
    public DateTime determineTimestamp(Resource resource, Representation representation) {
        return timestamp;
    }
    
    @Override
    public DateTime getBuildTimestamp() {
        return buildTimestamp;
    }
    
    @Override
    public DateTime getEarliestResourceTimestamp() {
        return modelTimestamp;
    }
    
    @Override
    public DateTime getLatestResourceTimestamp() {
        return modelTimestamp;
    }
    
    private DateTime loadBuildTimestamp() throws IOException {
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
        try {
            return new DateTime(buildTimestamp);
        } catch (IllegalArgumentException e) {
            /*
             * Probably means we are in Jenkins and maven.build.timestamp.format 
             * is being ignored, see https://issues.jenkins-ci.org/browse/JENKINS-9693
             * XXX delete this code when Jenkins is fixed!
             */
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMDD-HHmm")
                    .withZone(DateTimeZone.forID("Australia/Brisbane")); // Jenkins builds are always in Brisbane
            return formatter.parseDateTime(buildTimestamp);
        }
    }
    
    public static <T extends ReadableInstant> T maxInstant(T left, T right) {
        if (left.compareTo(right) < 0) return right;
        return left;
    }

}
