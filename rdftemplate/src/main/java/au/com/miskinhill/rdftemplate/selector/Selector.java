package au.com.miskinhill.rdftemplate.selector;

import java.util.List;

import com.hp.hpl.jena.rdf.model.RDFNode;

public interface Selector<T> {
    
    List<T> result(RDFNode node);
    
    T singleResult(RDFNode node);
    
    Class<T> getResultType();
    
    <Other> Selector<Other> withResultType(Class<Other> otherType);

}
