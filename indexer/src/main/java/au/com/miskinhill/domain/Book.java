package au.com.miskinhill.domain;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.apache.lucene.document.Document;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

public class Book extends GenericResource {

	public static final String TYPE = MHS.NS_URI + "Book";

    public Book(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}
    
    @Override
    public void addFieldsToDocument(String fieldNamePrefix, Document doc)
            throws UnknownLiteralTypeException, IOException, XMLStreamException {
        super.addFieldsToDocument(fieldNamePrefix, doc);
        
        Property dccreator = rdfResource.getModel().createProperty(DCTerms.NS, "creator");
        for (StmtIterator it = rdfResource.listProperties(dccreator); it.hasNext(); ) {
            Resource creator = it.nextStatement().getObject().as(Resource.class);
            GenericResource.fromRDF(creator, fulltextFetcher).addFieldsToDocument(
                    fieldNamePrefix + dccreator.getURI() + " ", doc);
        }
    }
	
	@Override
	protected String getAnchorText() {
	    throw new UnsupportedOperationException("Books are not top-level documents");
	}

    @Override
    protected String rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

}
