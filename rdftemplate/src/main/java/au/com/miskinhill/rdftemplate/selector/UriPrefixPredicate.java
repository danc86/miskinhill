package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class UriPrefixPredicate implements Predicate {
    
    private final String prefix;
    
    public UriPrefixPredicate(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public boolean evaluate(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply [uri-prefix] to non-resource node " + node);
        }
        return ((Resource) node).getURI().startsWith(prefix);
    }

}
