package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class UriAdaptation implements Adaptation<String> {
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }
    
    @Override
    public String adapt(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply #uri to non-resource node " + node);
        }
        return ((Resource) node).getURI();
    }

}
