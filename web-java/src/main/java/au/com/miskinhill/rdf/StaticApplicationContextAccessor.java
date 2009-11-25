package au.com.miskinhill.rdf;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** ughhhhhh lame */
@Component
public class StaticApplicationContextAccessor {
    
    private static ApplicationContext applicationContext;
    
    @Autowired
    public StaticApplicationContextAccessor(ApplicationContext applicationContext) {
        StaticApplicationContextAccessor.applicationContext = applicationContext;
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public static <T> T getBeanOfType(Class<T> type) {
        Map<String, T> candidateBeans = applicationContext.getBeansOfType(type);
        if (candidateBeans.entrySet().size() != 1)
            throw new RuntimeException("Expected to find exactly one bean of type " + type);
        return candidateBeans.values().iterator().next();
    }

}
