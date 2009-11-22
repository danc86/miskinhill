package au.com.miskinhill.rdf.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class FOAF {

	public static final String NS_URI = "http://xmlns.com/foaf/0.1/";
	
	public static final Resource Document = ResourceFactory.createResource(
	        NS_URI + "Document");
	
	public static final Property name = ResourceFactory.createProperty(
	        NS_URI, "name");
	public static final Property surname = ResourceFactory.createProperty(
	        NS_URI, "surname");
	
	///CLOVER:OFF
	private FOAF() {
    }
	///CLOVER:ON
	
}
