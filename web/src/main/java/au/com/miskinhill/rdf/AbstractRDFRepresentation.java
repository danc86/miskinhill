package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractRDFRepresentation implements Representation {
    
    private final Model bareModel;
    
    public AbstractRDFRepresentation(Model bareModel) {
        this.bareModel = bareModel;
    }

    @Override
    public String render(Resource resource) {
        SubgraphAccumulator acc = new SubgraphAccumulator(bareModel);
        acc.visit(bareModel.createResource(resource.getURI()));
        // XXX should also do defragged
        return renderSubgraph(acc.getSubgraph());
    }

    public abstract String renderSubgraph(Model subgraph);

}
