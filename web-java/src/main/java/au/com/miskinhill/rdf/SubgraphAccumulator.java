package au.com.miskinhill.rdf;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

class SubgraphAccumulator {
    
    private final Set<Resource> alreadyVisited = new HashSet<Resource>();
    private final Model subgraph = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
    
    public SubgraphAccumulator(Model original) {
        subgraph.setNsPrefixes(original.getNsPrefixMap());
    }
    
    public void visit(Resource resource) {
        if (alreadyVisited.contains(resource)) return;
        alreadyVisited.add(resource);
        for (StmtIterator it = resource.listProperties(); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            subgraph.add(stmt);
            if (stmt.getObject().isAnon()) {
                visit((Resource) stmt.getObject());
            }
        }
        for (StmtIterator it = resource.getModel().listStatements(null, null, resource); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            if (stmt.getSubject().isAnon()) {
                visit((Resource) stmt.getSubject());
            }
        }
    }
    
    public Model getSubgraph() {
        return subgraph;
    }
}