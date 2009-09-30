package au.com.miskinhill.rdftemplate.selector;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class NoopSelector extends AbstractSelector<RDFNode> {
    
    public NoopSelector() {
        super(RDFNode.class);
    }
    
    @Override
    public List<RDFNode> result(RDFNode node) {
        return Collections.singletonList(node);
    }

}
