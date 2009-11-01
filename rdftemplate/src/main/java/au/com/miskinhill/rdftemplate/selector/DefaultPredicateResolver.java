package au.com.miskinhill.rdftemplate.selector;

import java.util.HashMap;
import java.util.Map;

public class DefaultPredicateResolver implements PredicateResolver {
    
    private static final Map<String, Class<? extends Predicate>> PREDICATES = new HashMap<String, Class<? extends Predicate>>();
    static {
        PREDICATES.put("type", TypePredicate.class);
        PREDICATES.put("uri-prefix", UriPrefixPredicate.class);
    }

    @Override
    public Class<? extends Predicate> getByName(String name) {
        return PREDICATES.get(name);
    }

}
