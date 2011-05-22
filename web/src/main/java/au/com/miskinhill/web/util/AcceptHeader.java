package au.com.miskinhill.web.util;

import static java.lang.Math.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

/**
 * Based largely on webob/acceptparse.py.
 */
public class AcceptHeader {
    
    private static final Pattern PART_PATTERN = Pattern.compile(",\\s*([^\\s;,]+)(?:[^,]*?;\\s*q=([0-9.]*))?");
    
    // We de-prioritise these so that more specific types will beat these in spite of q values
    private static final Set<MediaType> NON_SPECIFIC_TYPES =
        new HashSet<MediaType>(Arrays.asList(
            MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    
    public static AcceptHeader parse(String value) {
        LinkedHashMap<MediaType, Float> result = new LinkedHashMap<MediaType, Float>();
        Matcher matcher = PART_PATTERN.matcher("," + value);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (name.equals("q"))
                continue;
            String quality = matcher.group(2);
            float qualityVal;
            if (StringUtils.isEmpty(quality)) {
                qualityVal = 1f;
            } else {
                try {
                    qualityVal = max(min(Float.parseFloat(quality), 1f), 0f);
                } catch (NumberFormatException e) {
                    qualityVal = 1f;
                }
            }
            result.put(MediaType.valueOf(name), qualityVal);
        }
        return new AcceptHeader(result);
    }
    
    private final LinkedHashMap<MediaType, Float> values;
    
    private AcceptHeader(LinkedHashMap<MediaType, Float> values) {
        this.values = values;
    }
    
    public boolean isAcceptable(String type) {
        return values.containsKey(type);
    }
    
    public MediaType bestMatch(List<MediaType> candidates) {
        float bestQuality = -1f;
        MediaType bestMatch = null;
        for (Map.Entry<MediaType, Float> entry: values.entrySet()) {
            for (MediaType candidate: candidates) {
                if (entry.getKey().includes(candidate) &&
                        (beats(candidate, bestMatch) || entry.getValue() > bestQuality)) {
                    bestMatch = candidate;
                    bestQuality = entry.getValue();
                }
            }
        }
        return bestMatch;
    }
    
    private boolean beats(MediaType newer, MediaType older) {
        return !NON_SPECIFIC_TYPES.contains(newer) &&
                NON_SPECIFIC_TYPES.contains(older);
    }

}
