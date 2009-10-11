package au.com.miskinhill.rdf.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class SIOC {

	public static final String NS_URI = "http://rdfs.org/sioc/ns#";
	
	public static final Resource Forum = ResourceFactory.createResource(
			NS_URI + "Forum");
	
	///CLOVER:OFF
	private SIOC() {
    }
	///CLOVER:ON
	
}
