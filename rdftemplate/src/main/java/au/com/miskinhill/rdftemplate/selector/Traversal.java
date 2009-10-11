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

import au.com.miskinhill.rdftemplate.NamespacePrefixMapper;

public class Traversal {
    
    private String propertyNamespacePrefix;
    private String propertyLocalName;
    private boolean inverse = false;
    private Predicate predicate;
    private Selector<? extends Comparable<?>> sortOrder;
    private Comparator<RDFNode> _sortComparator;
    private boolean reverseSorted = false;
    private Integer subscript;
    
    public List<RDFNode> traverse(RDFNode node) {
        if (!node.isResource()) {
            throw new SelectorEvaluationException("Attempted to traverse non-resource node " + node);
        }
        Resource resource = (Resource) node;
        Property property = resource.getModel().createProperty(
                NamespacePrefixMapper.getInstance().get(propertyNamespacePrefix),
                propertyLocalName);
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
        if (_sortComparator != null)
            Collections.sort(destinations, reverseSorted ? Collections.reverseOrder(_sortComparator) : _sortComparator);
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
                .append("propertyNamespacePrefix", propertyNamespacePrefix)
                .append("propertyLocalName", propertyLocalName)
                .append("inverse", inverse)
                .append("predicate", predicate)
                .append("sortOrder", sortOrder)
                .append("reverseSorted", reverseSorted)
                .append("subscript", subscript)
                .toString();
    }
    
    public String getPropertyLocalName() {
        return propertyLocalName;
    }
    
    public void setPropertyLocalName(String propertyLocalName) {
        this.propertyLocalName = propertyLocalName;
    }
    
    public String getPropertyNamespacePrefix() {
        return propertyNamespacePrefix;
    }
    
    public void setPropertyNamespacePrefix(String propertyNamespacePrefix) {
        this.propertyNamespacePrefix = propertyNamespacePrefix;
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
    
    public Selector<?> getSortOrder() {
        return sortOrder;
    }
    
    private static final class SelectorComparator<T extends Comparable<T>> implements Comparator<RDFNode> {
        private final Selector<T> selector;
        public SelectorComparator(Selector<T> selector) {
            this.selector = selector;
        }
        @Override
        public int compare(RDFNode left, RDFNode right) {
            T leftKey = selector.singleResult(left);
            T rightKey = selector.singleResult(right);
            return leftKey.compareTo(rightKey);
        }
    }
    
    public <T extends Comparable<T>> void setSortOrder(Selector<T> sortOrder) {
        this.sortOrder = sortOrder;
        this._sortComparator = new SelectorComparator<T>(sortOrder);
    }
    
    public boolean isReverseSorted() {
        return reverseSorted;
    }
    
    public void setReverseSorted(boolean reverseSorted) {
        this.reverseSorted = reverseSorted;
    }
    
    public Integer getSubscript() {
        return subscript;
    }
    
    public void setSubscript(Integer subscript) {
        this.subscript = subscript;
    }

}
