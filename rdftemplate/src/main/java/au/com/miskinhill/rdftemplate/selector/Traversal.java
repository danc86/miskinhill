package au.com.miskinhill.rdftemplate.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Traversal {
    
    private String propertyNamespace;
    private String propertyLocalName;
    private boolean inverse = false;
    private Predicate predicate;
    private List<Comparator<RDFNode>> sortOrder = new ArrayList<Comparator<RDFNode>>();
    private Integer subscript;
    
    private class SortComparator implements Comparator<RDFNode> {
        @Override
        public int compare(RDFNode left, RDFNode right) {
            for (Comparator<RDFNode> comparator: sortOrder) {
                int result = comparator.compare(left, right);
                if (result != 0)
                    return result;
            }
            return 0;
        }
    }
    
    public List<RDFNode> traverse(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to traverse non-resource node " + node);
        }
        Resource resource = (Resource) node;
        Property property = resource.getModel().createProperty(propertyNamespace, propertyLocalName);
        List<RDFNode> destinations = new ArrayList<RDFNode>();
        if (!inverse) {
            for (StmtIterator it = resource.listProperties(property); it.hasNext(); ) {
                destinations.add(it.nextStatement().getObject());
            }
        } else {
            for (ResIterator it = resource.getModel().listResourcesWithProperty(property, node); it.hasNext(); ) {
                destinations.add(it.nextResource());
            }
        }
        CollectionUtils.filter(destinations, predicate);
        if (!sortOrder.isEmpty())
            Collections.sort(destinations, new SortComparator());
        if (subscript != null) {
            if (destinations.size() <= subscript) {
                throw new SelectorEvaluationException("Cannot apply subscript " + subscript + " to nodes " + destinations);
            }
            destinations = Collections.singletonList(destinations.get(subscript));
        }
        return destinations;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("propertyNamespace", propertyNamespace)
                .append("propertyLocalName", propertyLocalName)
                .append("inverse", inverse)
                .append("predicate", predicate)
                .append("sortOrder", sortOrder)
                .append("subscript", subscript)
                .toString();
    }
    
    public String getPropertyLocalName() {
        return propertyLocalName;
    }
    
    public void setPropertyLocalName(String propertyLocalName) {
        this.propertyLocalName = propertyLocalName;
    }
    
    public String getPropertyNamespace() {
        return propertyNamespace;
    }
    
    public void setPropertyNamespace(String propertyNamespace) {
        this.propertyNamespace = propertyNamespace;
    }
    
    public boolean isInverse() {
        return inverse;
    }
    
    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }
    
    public Predicate getPredicate() {
        return predicate;
    }
    
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }
    
    public List<Comparator<RDFNode>> getSortOrder() {
        return sortOrder;
    }
    
    public void addSortOrderComparator(Comparator<RDFNode> selector) {
        this.sortOrder.add(selector);
    }
    
    public Integer getSubscript() {
        return subscript;
    }
    
    public void setSubscript(Integer subscript) {
        this.subscript = subscript;
    }

}
