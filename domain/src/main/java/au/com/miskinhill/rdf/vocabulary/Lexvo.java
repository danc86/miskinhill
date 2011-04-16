package au.com.miskinhill.rdf.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class Lexvo {

	public static final String NS_URI = "http://lexvo.org/ontology#";
	
	public static final Property iso6392BCode = ResourceFactory.createProperty(
	        NS_URI, "iso6392BCode");
	public static final Property iso639P1Code = ResourceFactory.createProperty(
	        NS_URI, "iso639P1Code");

	///CLOVER:OFF
	private Lexvo() {
    }
	///CLOVER:ON
	
}
