package au.com.miskinhill.rdf.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class BIBO {

    public static final String NS_URI = "http://purl.org/ontology/bibo/";

    public static final Property authorList = ResourceFactory.createProperty(NS_URI + "authorList");
    public static final Property contributorList = ResourceFactory.createProperty(NS_URI + "contributorList");
    public static final Property editor = ResourceFactory.createProperty(NS_URI + "editor");

    // /CLOVER:OFF
    private BIBO() {
    }
    // /CLOVER:ON

}
