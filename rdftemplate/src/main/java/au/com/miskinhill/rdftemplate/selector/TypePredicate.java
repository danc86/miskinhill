package au.com.miskinhill.rdftemplate.selector;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.lang.builder.ToStringBuilder;

import au.com.miskinhill.rdftemplate.NamespacePrefixMapper;

public class TypePredicate implements Predicate {
    
    private final String namespacePrefix;
    private final String localName;
    
    public TypePredicate(String namespacePrefix, String localName) {
        this.namespacePrefix = namespacePrefix;
        this.localName = localName;
    }
    
    public String getNamespacePrefix() {
        return namespacePrefix;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to apply [type] to non-resource node " + node);
        }
        Resource resource = (Resource) node;
        Resource type = resource.getModel().createResource(
                NamespacePrefixMapper.getInstance().get(namespacePrefix) + localName);
        for (Statement statement: (Set<Statement>) resource.listProperties(RDF.type).toSet()) {
            if (statement.getObject().equals(type))
                return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append(namespacePrefix).append(localName).toString();
    }

}
