package au.com.miskinhill.rdf;

import java.util.EnumSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractRDFRepresentation implements Representation {
    
    private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.HTMLAnchors, ShownIn.HTMLLinks, ShownIn.AtomLinks, ShownIn.Unapi);
    private final Model bareModel;
    
    public AbstractRDFRepresentation(Model bareModel) {
        this.bareModel = bareModel;
    }
    
    @Override
    public boolean isShownIn(ShownIn place) {
        return shownIn.contains(place);
    }

    @Override
    public String render(Resource resource) {
        SubgraphAccumulator acc = new SubgraphAccumulator(bareModel.createResource(resource.getURI()));
        acc.accumulate();
        return renderSubgraph(acc.getSubgraph());
    }

    public abstract String renderSubgraph(Model subgraph);

}
