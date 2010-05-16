package au.com.miskinhill.schema.sitemaps;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum ChangeFreq {

    ALWAYS("always"),
    HOURLY("hourly"),
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly"),
    NEVER("never");
    
    private final String value;

    ChangeFreq(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ChangeFreq fromValue(String v) {
        for (ChangeFreq c: ChangeFreq.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
