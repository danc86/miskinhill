package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.rdftemplate.XMLStream;

public interface XMLStreamRepresentation extends Representation {
    
    public XMLStream renderXMLStream(Resource resource);

}
