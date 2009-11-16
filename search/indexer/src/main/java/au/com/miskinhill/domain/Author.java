package au.com.miskinhill.domain;

import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.rdf.vocabulary.FOAF;
import au.com.miskinhill.rdf.vocabulary.MHS;

public class Author extends GenericResource {

	public static final String TYPE = MHS.NS_URI + "Author";

    public Author(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}
	
	@Override
	protected String getAnchorText() {
	    return toHTML(rdfResource.getRequiredProperty(FOAF.name).getLiteral());
	}

    @Override
    protected String rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

}
