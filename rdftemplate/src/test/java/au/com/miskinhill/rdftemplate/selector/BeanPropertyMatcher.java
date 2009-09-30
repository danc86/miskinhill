package au.com.miskinhill.rdftemplate.selector;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class BeanPropertyMatcher<T> extends BaseMatcher<T> {
    
    private final Class<? extends T> matchedType;
    private final Map<String, Matcher<?>> requiredProperties = new LinkedHashMap<String, Matcher<?>>();
    
    public BeanPropertyMatcher(Class<? extends T> type) {
        this.matchedType = type;
    }
    
    public void addRequiredProperty(String name, Matcher<?> matcher) {
        requiredProperties.put(name, matcher);
    }
    
    @Override
    public boolean matches(Object arg0) {
        if (!matchedType.isInstance(arg0))
            return false;
        for (Map.Entry<String, Matcher<?>> property: requiredProperties.entrySet()) {
            try {
                Object beanProperty = PropertyUtils.getProperty(arg0, property.getKey());
                if (!property.getValue().matches(beanProperty))
                    return false;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    @Override
    public void describeTo(Description desc) {
        desc.appendText(matchedType.getName());
        desc.appendText("[");
        boolean first = true;
        for (Map.Entry<String, Matcher<?>> property: requiredProperties.entrySet()) {
            if (!first)
                desc.appendText(",");
            desc.appendText(property.getKey());
            desc.appendText("=");
            property.getValue().describeTo(desc);
            first = false;
        }
        desc.appendText("]");
    }

}
