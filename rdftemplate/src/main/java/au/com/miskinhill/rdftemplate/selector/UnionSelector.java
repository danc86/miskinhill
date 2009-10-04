package au.com.miskinhill.rdftemplate.selector;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.builder.ToStringBuilder;

public class UnionSelector<T> extends AbstractSelector<T> {
    
    private final List<Selector<? extends T>> selectors;
    
    public UnionSelector(List<Selector<? extends T>> selectors) {
        super(null);
        this.selectors = selectors;
    }
    
    @Override
    public List<T> result(RDFNode node) {
        LinkedHashSet<T> results = new LinkedHashSet<T>();
        for (Selector<? extends T> selector: selectors) {
            results.addAll(selector.result(node));
        }
        return new ArrayList<T>(results);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(selectors).toString();
    }
    
    public List<Selector<? extends T>> getSelectors() {
        return selectors;
    }
    
    @Override
    public <Other> Selector<Other> withResultType(Class<Other> otherType) {
        for (Selector<? extends T> selector: selectors) {
            selector.withResultType(otherType); // class cast exception?
        }
        return (Selector<Other>) this;
    }
    
}
