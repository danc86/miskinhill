package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.equalTo;

public class PredicateMatcher<T extends Predicate> extends BeanPropertyMatcher<T> {
    
    private PredicateMatcher(Class<T> type) {
        super(type);
    }
    
    public static PredicateMatcher<UriPrefixPredicate> uriPrefixPredicate(String prefix) {
        PredicateMatcher<UriPrefixPredicate> m = new PredicateMatcher<UriPrefixPredicate>(UriPrefixPredicate.class);
        m.addRequiredProperty("prefix", equalTo(prefix));
        return m;
    }

}
