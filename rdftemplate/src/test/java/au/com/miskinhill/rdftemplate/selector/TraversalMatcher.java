package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Comparator;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.hamcrest.Matcher;

public class TraversalMatcher extends BeanPropertyMatcher<Traversal> {
    
    private TraversalMatcher() {
        super(Traversal.class);
    }
    
    public static TraversalMatcher traversal(String propertyNamespacePrefix, String propertyLocalName) {
        TraversalMatcher m = new TraversalMatcher();
        m.addRequiredProperty("propertyNamespacePrefix", equalTo(propertyNamespacePrefix));
        m.addRequiredProperty("propertyLocalName", equalTo(propertyLocalName));
        return m;
    }
    
    public TraversalMatcher inverse() {
        addRequiredProperty("inverse", equalTo(true));
        return this;
    }
    
    public TraversalMatcher withPredicate(Matcher<? extends Predicate> predicate) {
        addRequiredProperty("predicate", predicate);
        return this;
    }
    
    public TraversalMatcher withSortOrder(Matcher<? extends Comparator<RDFNode>>... sortOrder) {
        addRequiredProperty("sortOrder", hasItems(sortOrder));
        return this;
    }
    
    public TraversalMatcher withSubscript(int subscript) {
        addRequiredProperty("subscript", equalTo(subscript));
        return this;
    }

}
