package au.com.miskinhill.rdftemplate.selector;

import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

public abstract class AbstractSelector<T> implements Selector<T> {
    
    private final Class<T> resultType;
    
    protected AbstractSelector(Class<T> resultType) {
        this.resultType = resultType;
    }
    
    @Override
    public abstract List<T> result(RDFNode node);
    
    @Override
    public T singleResult(RDFNode node) {
        List<T> results = result(node);
        if (results.size() != 1) {
            throw new SelectorEvaluationException("Expected exactly one result but got " + results);
        }
        return results.get(0);
    }
    
    @Override
    public Class<T> getResultType() {
        return resultType;
    }
    
    @Override
    public <Other> Selector<Other> withResultType(Class<Other> otherType) {
        if (!otherType.isAssignableFrom(resultType)) {
            throw new ClassCastException("Result type " + resultType + " incompatible with requested type " + otherType);
        }
        return (Selector<Other>) this;
    }

}