package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import au.com.miskinhill.rdftemplate.selector.Adaptation;
import au.com.miskinhill.rdftemplate.selector.SelectorEvaluationException;

public class LCSHCleanupAdaptation implements Adaptation<String> {
    
    @Override
    public String adapt(RDFNode node) {
        if (!node.isLiteral())
            throw new SelectorEvaluationException("Attempted to apply #lcsh-cleanup to non-literal node " + node);
        Literal literal = node.as(Literal.class);
        if (literal.getDatatype() != null)
            throw new SelectorEvaluationException("Attempted to apply #lcsh-cleanup to node with datatype " + literal.getDatatype());
        return literal.getValue().toString().replaceAll("--", " \u2013 ");
    }

    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }

}
