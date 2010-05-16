package au.com.miskinhill.web.util;

import static java.lang.Math.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Based largely on webob/acceptparse.py.
 */
public class AcceptHeader {
    
    private static final Pattern PART_PATTERN = Pattern.compile(",\\s*([^\\s;,]+)(?:[^,]*?;\\s*q=([0-9.]*))?");
    
    public static AcceptHeader parse(String value) {
        LinkedHashMap<String, Float> result = new LinkedHashMap<String, Float>();
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
            result.put(name, qualityVal);
        }
        return new AcceptHeader(result);
    }
    
    private final LinkedHashMap<String, Float> values;
    
    private AcceptHeader(LinkedHashMap<String, Float> values) {
        this.values = values;
    }
    
    public boolean isAcceptable(String type) {
        return values.containsKey(type);
    }
    
    public String bestMatch(List<String> candidates) {
        float bestQuality = -1f;
        String bestMatch = null;
        for (String candidate: candidates) {
            for (Map.Entry<String, Float> entry: values.entrySet()) {
                if (entry.getValue() <= bestQuality)
                    continue;
                if (entry.getKey().equalsIgnoreCase(candidate) || entry.getKey().equals("*")) {
                    bestQuality = entry.getValue();
                    bestMatch = entry.getKey();
                }
            }
        }
        return bestMatch;
    }

}
