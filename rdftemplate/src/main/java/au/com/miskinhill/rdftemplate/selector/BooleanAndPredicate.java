package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.builder.ToStringBuilder;

public class BooleanAndPredicate implements Predicate {
    
    private final Predicate left;
    private final Predicate right;
    
    public BooleanAndPredicate(Predicate left, Predicate right) {
        this.left = left;
        this.right = right;
    }
    
    public Predicate getLeft() {
        return left;
    }
    
    public Predicate getRight() {
        return right;
    }
    
    @Override
    public boolean evaluate(RDFNode node) {
        return left.evaluate(node) && right.evaluate(node);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(left).append(right).toString();
    }

}
