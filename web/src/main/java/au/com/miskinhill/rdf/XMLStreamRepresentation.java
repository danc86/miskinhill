package au.com.miskinhill.rdf;

import java.net.URI;

import com.hp.hpl.jena.rdf.model.Resource;

import au.id.djc.rdftemplate.XMLStream;

public interface XMLStreamRepresentation extends Representation {
    
    URI getXMLNamespace();
    
    URI getXSD();
    
    XMLStream renderXMLStream(Resource resource);

}
