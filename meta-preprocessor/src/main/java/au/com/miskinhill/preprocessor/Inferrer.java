package au.com.miskinhill.preprocessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Inferrer {
    
    private static final Logger LOG = Logger.getLogger(Inferrer.class.getName());
    
    private final Model m;
    
    public Inferrer(Model m) {
        this.m = m;
    }
    
    public void apply(Model destination) {
        LOG.info("Applying transitive superclasses");
        Map<Resource, Set<Resource>> transitiveSuperclasses = transitiveSuperclasses();
        for (Statement stmt: m.listStatements(null, RDF.type, (RDFNode) null).toList()) {
            for (Resource superclass: transitiveSuperclasses.get((Resource) stmt.getObject()))
                destination.add(ResourceFactory.createStatement(stmt.getSubject(), RDF.type, superclass));
        }
        LOG.info("Destination model contains " + destination.getGraph().size() + " triples");
        
        LOG.info("Applying transitive superproperties");
        Map<Property, Set<Property>> transitiveSuperproperties = transitiveSuperproperties();
        for (Statement stmt: m.listStatements().toList()) {
            for (Property superproperty: transitiveSuperproperties.get(stmt.getPredicate()))
                destination.add(ResourceFactory.createStatement(stmt.getSubject(), superproperty, stmt.getObject()));
        }
        LOG.info("Destination model contains " + destination.getGraph().size() + " triples");
        
        LOG.info("Applying inverse properties");
        Map<Property, Set<Property>> inverseProperties = inverseProperties();
        for (Statement stmt: m.listStatements().toList()) {
            if (inverseProperties.get(stmt.getPredicate()) != null)
                for (Property inverse: inverseProperties.get(stmt.getPredicate()))
                    destination.add(ResourceFactory.createStatement((Resource) stmt.getObject(), inverse, stmt.getSubject()));
        }
        LOG.info("Destination model contains " + destination.getGraph().size() + " triples");
    }
    
    private Set<Resource> transitiveObjects(Resource subject, Property predicate) {
        Set<Resource> result = new HashSet<Resource>();
        Set<Resource> directObjects = new HashSet<Resource>();
        for (NodeIterator it = m.listObjectsOfProperty(subject, predicate); it.hasNext(); )
            directObjects.add((Resource) it.nextNode());
        result.addAll(directObjects);
        for (Resource directObject: directObjects)
            result.addAll(transitiveObjects(directObject, predicate));
        return result;
    }
    
    /** Returns map of RDF class to set of all other classes implied by having that class. */
    private Map<Resource, Set<Resource>> transitiveSuperclasses() {
        Map<Resource, Set<Resource>> result = new HashMap<Resource, Set<Resource>>();
        for (NodeIterator it = m.listObjectsOfProperty(RDF.type); it.hasNext(); ) {
            Resource cls = (Resource) it.nextNode();
            result.put(cls, transitiveObjects(cls, RDFS.subClassOf));
        }
        return result;
    }
    
    /** Returns map of RDF property to set of all other properties implied by that property. */
    private Map<Property, Set<Property>> transitiveSuperproperties() {
        Map<Property, Set<Property>> result = new HashMap<Property, Set<Property>>();
        for (StmtIterator it = m.listStatements(); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            if (!result.containsKey(stmt.getPredicate())) {
                Set<Property> superProperties = new HashSet<Property>();
                for (Resource superProperty: transitiveObjects(stmt.getPredicate(), RDFS.subPropertyOf))
                    superProperties.add((Property) superProperty.as(Property.class));
                result.put(stmt.getPredicate(), superProperties);
            }
        }
        return result;
    }
    
    private Map<Property, Set<Property>> inverseProperties() {
        Map<Property, Set<Property>> result = new HashMap<Property, Set<Property>>();
        for (StmtIterator it = m.listStatements(null, OWL.inverseOf, (RDFNode) null); it.hasNext(); ) {
            Statement stmt = it.nextStatement();
            Property p1 = (Property) stmt.getSubject().as(Property.class);
            Property p2 = (Property) stmt.getObject().as(Property.class);
            if (!result.containsKey(p1))
                result.put(p1, new HashSet<Property>());
            if (!result.containsKey(p2))
                result.put(p2, new HashSet<Property>());
            result.get(p1).add(p2);
            result.get(p2).add(p1);
        }
        return result;
    }

}
