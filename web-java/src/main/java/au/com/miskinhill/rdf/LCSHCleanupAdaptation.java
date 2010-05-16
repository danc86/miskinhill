package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;

import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

public class LCSHCleanupAdaptation extends AbstractAdaptation<String, Literal> {
    
    public LCSHCleanupAdaptation() {
        super(String.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected String doAdapt(Literal literal) {
        if (literal.getDatatype() != null)
            throw new SelectorEvaluationException("Attempted to apply #lcsh-cleanup to node with datatype " + literal.getDatatype());
        return literal.getValue().toString().replaceAll("--", " \u2013 ");
    }

}
