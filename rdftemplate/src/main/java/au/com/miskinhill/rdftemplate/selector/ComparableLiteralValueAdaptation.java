package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ComparableLiteralValueAdaptation implements Adaptation<Comparable> {
    
    @Override
    public Class<Comparable> getDestinationType() {
        return Comparable.class;
    }

    @Override
    public Comparable<?> adapt(RDFNode node) {
        if (!node.isLiteral()) {
            throw new SelectorEvaluationException("Attempted to apply #comparable-lv to non-literal node " + node);
        }
        Object literalValue = ((Literal) node).getValue();
        if (!(literalValue instanceof Comparable<?>)) {
            throw new SelectorEvaluationException("Attempted to apply #comparable-lv to non-Comparable node " + node +
                    " with literal value of type " + literalValue.getClass());
        }
        return (Comparable<?>) literalValue;
    }

}
