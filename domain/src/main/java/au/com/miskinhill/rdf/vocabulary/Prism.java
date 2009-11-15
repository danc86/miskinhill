package au.com.miskinhill.rdf.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class Prism {

	public static final String NS_URI = "http://prismstandard.org/namespaces/1.2/basic/";
	
	public static final Property publicationDate = ResourceFactory.createProperty(
			NS_URI + "publicationDate");
	
	///CLOVER:OFF
	private Prism() {
    }
	///CLOVER:ON
	
}
