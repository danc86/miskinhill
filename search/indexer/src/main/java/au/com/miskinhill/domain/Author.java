package au.com.miskinhill.domain;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class Author extends GenericResource {

	public Author(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}
	
	@Override
	protected Property anchorProperty() {
		return FOAF.name;
	}

}
