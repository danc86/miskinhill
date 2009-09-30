package au.com.miskinhill.rdftemplate.selector;

import com.hp.hpl.jena.rdf.model.RDFNode;

public interface Adaptation<T> {

    Class<T> getDestinationType();
    
    T adapt(RDFNode node);

}
