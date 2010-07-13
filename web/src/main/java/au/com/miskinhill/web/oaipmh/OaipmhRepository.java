package au.com.miskinhill.web.oaipmh;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.miskinhill.rdf.vocabulary.MHS;

/**
 * Responsible for deciding which of the resources in our model are exposed to
 * OAI-PMH harvesters as being part of this "repository".
 */
@Component
public class OaipmhRepository {
    
    private final Model model;
    
    @Autowired
    public OaipmhRepository(Model model) {
        this.model = model;
    }
    
    /**
     * Returns a {@link Resource} for the requested URI, or null if it does not
     * exist as far as OAI-PMH is concerned.
     */
    public Resource getResourceInRepository(String uri) {
        Resource resource = model.createResource(uri);
        if (resource.hasProperty(RDF.type, MHS.Article) && REPOSITORY_FILTER.accept(resource))
            return resource;
        return null;
    }
    
    public Iterator<Resource> getAllResourcesInRepository() {
        return model.listSubjectsWithProperty(RDF.type, MHS.Article).filterKeep(REPOSITORY_FILTER);
    }
            
    private static final Filter<Resource> REPOSITORY_FILTER = new Filter<Resource>() {
        @Override
        public boolean accept(Resource o) {
            Resource issue = o.getProperty(DCTerms.isPartOf).getObject().as(Resource.class);
            return issue.getURI().startsWith("http://miskinhill.com.au/journals/");
        }
    };

}
