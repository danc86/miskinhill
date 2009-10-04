package au.com.miskinhill.rdftemplate.selector;

import static org.junit.matchers.JUnitMatchers.hasItems;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.hamcrest.Matcher;

public class SelectorMatcher<T extends Selector<?>> extends BeanPropertyMatcher<T> {

    private SelectorMatcher(Class<? extends T> type) {
        super(type);
    }
    
    public static SelectorMatcher<Selector<RDFNode>> selector(Matcher<Traversal>... traversals) {
        if (traversals.length == 0) {
            return new SelectorMatcher<Selector<RDFNode>>(NoopSelector.class);
        }
        SelectorMatcher<Selector<RDFNode>> m = new SelectorMatcher<Selector<RDFNode>>(TraversingSelector.class);
        m.addRequiredProperty("traversals", hasItems(traversals));
        return m;
    }
    
    public static <R> SelectorMatcher<UnionSelector<R>> unionSelector(Matcher<Selector<R>>... selectors) {
        SelectorMatcher<UnionSelector<R>> m = new SelectorMatcher(UnionSelector.class);
        m.addRequiredProperty("selectors", hasItems(selectors));
        return m;
    }
    
    public <A> SelectorMatcher<Selector<?>> withAdaptation(Matcher<? extends Adaptation<A>> adaptation) {
        SelectorMatcher<Selector<?>> m = new SelectorMatcher(SelectorWithAdaptation.class);
        m.addRequiredProperty("baseSelector", this);
        m.addRequiredProperty("adaptation", adaptation);
        return m;
    }
    
}
