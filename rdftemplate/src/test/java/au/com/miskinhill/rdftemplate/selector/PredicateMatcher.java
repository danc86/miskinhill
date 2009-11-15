package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.equalTo;

import org.hamcrest.Matcher;

public class PredicateMatcher<T extends Predicate> extends BeanPropertyMatcher<T> {
    
    private PredicateMatcher(Class<T> type) {
        super(type);
    }
    
    public static PredicateMatcher<UriPrefixPredicate> uriPrefixPredicate(String prefix) {
        PredicateMatcher<UriPrefixPredicate> m = new PredicateMatcher<UriPrefixPredicate>(UriPrefixPredicate.class);
        m.addRequiredProperty("prefix", equalTo(prefix));
        return m;
    }
    
    public static PredicateMatcher<TypePredicate> typePredicate(String namespace, String localName) {
        PredicateMatcher<TypePredicate> m = new PredicateMatcher<TypePredicate>(TypePredicate.class);
        m.addRequiredProperty("namespace", equalTo(namespace));
        m.addRequiredProperty("localName", equalTo(localName));
        return m;
    }
    
    public static PredicateMatcher<BooleanAndPredicate> booleanAndPredicate(
            Matcher<? extends Predicate> left, Matcher<? extends Predicate> right) {
        PredicateMatcher<BooleanAndPredicate> m = new PredicateMatcher<BooleanAndPredicate>(BooleanAndPredicate.class);
        m.addRequiredProperty("left", left);
        m.addRequiredProperty("right", right);
        return m;
    }

}
