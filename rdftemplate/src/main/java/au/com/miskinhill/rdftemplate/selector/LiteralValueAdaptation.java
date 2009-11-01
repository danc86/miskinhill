package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class LiteralValueAdaptation implements Adaptation<Object> {
    
    @Override
    public Class<Object> getDestinationType() {
        return Object.class;
    }

    @Override
    public Object adapt(RDFNode node) {
        if (!node.isLiteral()) {
            throw new SelectorEvaluationException("Attempted to apply #lv to non-literal node " + node);
        }
        return ((Literal) node).getValue();
    }

}
