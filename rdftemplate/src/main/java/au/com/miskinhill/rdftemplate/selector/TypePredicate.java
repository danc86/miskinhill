package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.lang.builder.ToStringBuilder;

public class TypePredicate implements Predicate {
    
    private final String namespace;
    private final String localName;
    
    public TypePredicate(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    @Override
    public boolean evaluate(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply [type] to non-resource node " + node);
        }
        Resource resource = (Resource) node;
        Resource type = resource.getModel().createResource(namespace + localName);
        for (Statement statement: resource.listProperties(RDF.type).toSet()) {
            if (statement.getObject().equals(type))
                return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(namespace).append(localName).toString();
    }

}
