package au.com.miskinhill.rdftemplate.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TraversingSelector extends AbstractSelector<RDFNode> {
    
    private final List<Traversal> traversals = new ArrayList<Traversal>();
    
    public TraversingSelector() {
        super(RDFNode.class);
    }
    
    @Override
    public List<RDFNode> result(RDFNode node) {
        Set<RDFNode> current = Collections.singleton(node);
        for (Traversal traversal: traversals) {
            LinkedHashSet<RDFNode> destinationsUnion = new LinkedHashSet<RDFNode>();
            for (RDFNode start: current) {
                destinationsUnion.addAll(traversal.traverse(start));
            }
            current = destinationsUnion;
        }
        return new ArrayList<RDFNode>(current);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(traversals).toString();
    }
    
    public List<Traversal> getTraversals() {
        return traversals;
    }
    
    public void addTraversal(Traversal traversal) {
        traversals.add(traversal);
    }
    
}
