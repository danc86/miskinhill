package au.com.miskinhill.rdf;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

class SubgraphAccumulator implements RDFVisitor {
    
    private final Resource start;
    private final String startUriWithFrag;
    private final Set<Resource> alreadyVisited = new HashSet<Resource>();
    private final Model subgraph = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
    
    public SubgraphAccumulator(Resource start) {
        this.start = start;
        this.startUriWithFrag = start.getURI() + "#";
        subgraph.setNsPrefixes(start.getModel().getNsPrefixMap());
    }
    
    public void accumulate() {
        visit(start);
    }
    
    public void visit(Resource resource) {
        if (alreadyVisited.contains(resource)) return;
        alreadyVisited.add(resource);
        for (StmtIterator it = resource.listProperties(); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            subgraph.add(stmt);
            stmt.getObject().visitWith(this);
        }
        for (StmtIterator it = resource.getModel().listStatements(null, null, resource); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            stmt.getSubject().visitWith(this);
        }
    }
    
    @Override
    public Object visitURI(Resource r, String uri) {
        if (uri.startsWith(startUriWithFrag))
            visit(r);
        return null;
    }
    
    @Override
    public Object visitLiteral(Literal l) {
        return null;
    }
    
    @Override
    public Object visitBlank(Resource r, AnonId id) {
        visit(r);
        return null;
    }
    
    public Model getSubgraph() {
        return subgraph;
    }
}