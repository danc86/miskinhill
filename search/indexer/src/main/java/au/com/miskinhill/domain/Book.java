package au.com.miskinhill.domain;

import au.com.miskinhill.domain.vocabulary.MHS;

import com.hp.hpl.jena.rdf.model.Resource;

public class Book extends GenericResource {

	public static final Resource TYPE = MHS.Book;

    public Book(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}
	
	@Override
	protected String getAnchorText() {
	    throw new UnsupportedOperationException("Books are not top-level documents");
	}

    @Override
    protected Resource rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
