package au.com.miskinhill.rdftemplate;

import java.util.HashMap;

import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.junit.Ignore;

@Ignore // why does JUnit think this is a test?
public final class TestNamespacePrefixMap extends HashMap<String, String> {
    
    public static final String MHS_NS = "http://miskinhill.com.au/rdfschema/1.0/";
    public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
    
    private static final long serialVersionUID = 2119318190108418683L;
    
    private static final TestNamespacePrefixMap instance = new TestNamespacePrefixMap();
    public static TestNamespacePrefixMap getInstance() {
        return instance;
    }
    
    private TestNamespacePrefixMap() {
        put("mhs", MHS_NS);
        put("dc", DCTerms.NS);
        put("foaf", FOAF_NS);
        put("rdf", RDF.getURI());
        put("sioc", "http://rdfs.org/sioc/ns#");
        put("lingvoj", "http://www.lingvoj.org/ontology#");
    }
    
}
