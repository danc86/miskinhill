package au.com.miskinhill.rdftemplate.selector;


import static org.junit.matchers.JUnitMatchers.hasItems;

import org.hamcrest.Matcher;

public class SelectorMatcher<T extends Selector<?>> extends BeanPropertyMatcher<T> {

    private SelectorMatcher(Class<? extends T> type) {
        super(type);
    }
    
    public static SelectorMatcher<Selector<?>> selector(Matcher<Traversal>... traversals) {
        if (traversals.length == 0) {
            return new SelectorMatcher<Selector<?>>(NoopSelector.class);
        }
        SelectorMatcher<Selector<?>> m = new SelectorMatcher<Selector<?>>(TraversingSelector.class);
        m.addRequiredProperty("traversals", hasItems(traversals));
        return m;
    }
    
    public <A> SelectorMatcher<Selector<?>> withAdaptation(Matcher<? extends Adaptation<A>> adaptation) {
        SelectorMatcher<Selector<?>> m = new SelectorMatcher(SelectorWithAdaptation.class);
        m.addRequiredProperty("baseSelector", this);
        m.addRequiredProperty("adaptation", adaptation);
        return m;
    }
    
}
