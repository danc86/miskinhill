package au.com.miskinhill.rdf;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public final class RDFUtil {

    ///CLOVER:OFF
    private RDFUtil() {
    }
    ///CLOVER:ON
    
    public static Set<Resource> getTypes(Resource resource) {
        HashSet<Resource> result = new HashSet<Resource>();
        for (StmtIterator it = resource.listProperties(RDF.type); it.hasNext(); )
            result.add(it.nextStatement().getObject().as(Resource.class));
        return result;
    }

    public static boolean hasAnyType(Resource resource, Set<Resource> types) {
        for (StmtIterator it = resource.listProperties(RDF.type); it.hasNext(); ) {
            Resource type = it.nextStatement().getObject().as(Resource.class);
            if (types.contains(type))
                return true;
        }
        return false;
    }

}
