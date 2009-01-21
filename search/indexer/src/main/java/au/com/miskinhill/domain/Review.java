package au.com.miskinhill.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import au.com.miskinhill.domain.vocabulary.MHS;
import au.com.miskinhill.search.analysis.MHAnalyzer;
import au.com.miskinhill.search.analysis.XMLTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;

public class Review extends GenericResource {
    
    public static final Resource TYPE = MHS.Review;
	
	public Review(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		super(rdfResource, fulltextFetcher);
	}

	private static final byte[] XHTML_STRICT_DTD_DECL = 
			("<!DOCTYPE html " +
			"PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
			"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
			.getBytes();
	
	@Override
	public void addFieldsToDocument(String fieldNamePrefix, Document doc)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		super.addFieldsToDocument(fieldNamePrefix, doc);
		
		if (!rdfResource.getURI().substring(0, 24).equals("http://miskinhill.com.au"))
			throw new IllegalArgumentException("Cannot fetch content which is not under http://miskinhill.com.au");
		doc.add(new Field(fieldNamePrefix + "content", new XMLTokenizer(
				new SequenceInputStream(
					new ByteArrayInputStream(XHTML_STRICT_DTD_DECL), 
					fulltextFetcher.fetch(rdfResource.getURI().substring(24) + ".html")), 
				new MHAnalyzer())));
	}
	
	@Override
	protected String getAnchorText() {
	    StmtIterator i = rdfResource.listProperties(MHS.reviews);
	    if (!i.hasNext())
	        throw new IllegalArgumentException("Review does not review anything");
	    Resource reviewed = (Resource) i.nextStatement().getObject().as(Resource.class);
	    if (i.hasNext())
	        throw new IllegalArgumentException("Review reviews more than one thing");
		Property dctitle = rdfResource.getModel().createProperty(DCTerms.NS, "title");
		Property dccreator = rdfResource.getModel().createProperty(DCTerms.NS, "creator");
		Property dcdate = rdfResource.getModel().createProperty(DCTerms.NS, "date");
		return reviewed.getRequiredProperty(dccreator).getString() + ", <em>" + 
		        reviewed.getRequiredProperty(dctitle).getString() + "</em> (" + 
		        reviewed.getRequiredProperty(dcdate).getString().substring(0, 4) + ")"; 
	}

    @Override
    protected Resource rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

}
