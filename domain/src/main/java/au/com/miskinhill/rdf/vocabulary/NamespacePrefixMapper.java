package au.com.miskinhill.rdf.vocabulary;

import java.util.HashMap;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public final class NamespacePrefixMapper extends HashMap<String, String> {
    
    private static final long serialVersionUID = 2119318190108418682L;
    
    private static final NamespacePrefixMapper instance = new NamespacePrefixMapper();
    public static NamespacePrefixMapper getInstance() {
        return instance;
    }
    
    private NamespacePrefixMapper() {
        put("mhs", "http://miskinhill.com.au/rdfschema/1.0/");
        put("dc", DCTerms.NS);
        put("old-dc", DC_11.NS);
        put("foaf", FOAF.NS);
        put("rdf", RDF.getURI());
        put("rdfs", RDFS.getURI());
        put("xs", XSD.getURI());
        put("xsd", "http://www.w3.org/TR/xmlschema-2/#");
        put("contact", "http://www.w3.org/2000/10/swap/pim/contact#");
        put("geonames", "http://www.geonames.org/ontology#");
        put("sioc", "http://rdfs.org/sioc/ns#");
        put("awol", "http://bblfish.net/work/atom-owl/2006-06-06/#");
        put("lingvoj", "http://www.lingvoj.org/ontology#");
        put("prism", "http://prismstandard.org/namespaces/1.2/basic/");
        put("owl", OWL.NS);
        put("rev", "http://purl.org/stuff/rev#");
        put("skos", "http://www.w3.org/2004/02/skos/core#");
        put("vs", "http://www.w3.org/2003/06/sw-vocab-status/ns#");
        put("wot", "http://xmlns.com/wot/0.1/");
        put("dcmitype", "http://purl.org/dc/dcmitype/");
        put("vann", "http://purl.org/vocab/vann/");
        put("cc", "http://web.resource.org/cc/");
        put("dcam", "http://purl.org/dc/dcam/");
        put("grddl", "http://www.w3.org/2003/g/data-view#");
        put("wordnet", "http://xmlns.com/wordnet/1.6/");
        put("wgs84", "http://www.w3.org/2003/01/geo/wgs84_pos#");
    }
    
}
