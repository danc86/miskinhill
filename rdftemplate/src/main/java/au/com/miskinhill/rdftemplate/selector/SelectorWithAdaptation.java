package au.com.miskinhill.rdftemplate.selector;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SelectorWithAdaptation<T> extends AbstractSelector<T> {
    
    private final Selector<RDFNode> baseSelector;
    private final Adaptation<T> adaptation;
    
    public SelectorWithAdaptation(Selector<RDFNode> baseSelector, Adaptation<T> adaptation) {
        super(adaptation.getDestinationType());
        this.baseSelector = baseSelector;
        this.adaptation = adaptation;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(baseSelector).append(adaptation).toString();
    }
    
    @Override
    public List<T> result(RDFNode node) {
        List<RDFNode> baseResults = baseSelector.result(node);
        List<T> results = new ArrayList<T>();
        for (RDFNode resultNode: baseResults) {
            results.add(adaptation.adapt(resultNode));
        }
        return results;
    }
    
    @Override
    public T singleResult(RDFNode node) {
        return adaptation.adapt(baseSelector.singleResult(node));
    }
    
    public Selector<RDFNode> getBaseSelector() {
        return baseSelector;
    }
    
    public Adaptation<T> getAdaptation() {
        return adaptation;
    }

}