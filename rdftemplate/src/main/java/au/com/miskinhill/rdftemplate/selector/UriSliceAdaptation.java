package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class UriSliceAdaptation implements Adaptation<String> {
    
    private final Integer startIndex;
    
    public UriSliceAdaptation(Integer startIndex) {
        this.startIndex = startIndex;
    }
    
    public Integer getStartIndex() {
        return startIndex;
    }
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }

    @Override
    public String adapt(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply #uri-slice to non-resource node " + node);
        }
        String uri = ((Resource) node).getURI();
        return uri.substring(startIndex);
    }

}
