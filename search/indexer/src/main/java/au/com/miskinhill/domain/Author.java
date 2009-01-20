package au.com.miskinhill.domain;

import au.com.miskinhill.domain.vocabulary.MHS;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class Author extends GenericResource {

	public static final Resource TYPE = MHS.Author;

    public Author(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}
	
	@Override
	protected Property anchorProperty() {
		return FOAF.name;
	}

    @Override
    protected Resource rdfType() {
        return TYPE;
    }

}
