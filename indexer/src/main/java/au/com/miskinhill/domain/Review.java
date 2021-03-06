package au.com.miskinhill.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;
import au.com.miskinhill.rdf.vocabulary.FOAF;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.search.analysis.MHAnalyzers;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;
import au.com.miskinhill.search.analysis.XMLTokenizer;

public class Review extends GenericResource {
    
    public static final String TYPE = MHS.NS_URI + "Review";
	
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
		
		for (Resource reviewedRDF: findReviewed()) {
    		GenericResource reviewed = GenericResource.fromRDF(reviewedRDF, fulltextFetcher);
            reviewed.addFieldsToDocument(fieldNamePrefix + MHS.reviews.getURI() + " ", doc);
		}
		
		if (!rdfResource.getURI().substring(0, 24).equals("http://miskinhill.com.au"))
			throw new IllegalArgumentException("Cannot fetch content which is not under http://miskinhill.com.au");
		doc.add(new Field(fieldNamePrefix + "content", new XMLTokenizer(
				new SequenceInputStream(
					new ByteArrayInputStream(XHTML_STRICT_DTD_DECL), 
					fulltextFetcher.fetchFulltext(rdfResource.getURI().substring(24) + ".html")), 
				MHAnalyzers.getAnalyzerMap())));
	}
	
	@Override
	protected String getAnchorText() {
		Property dctitle = rdfResource.getModel().createProperty(DCTerms.NS, "title");
		Property dccreator = rdfResource.getModel().createProperty(DCTerms.NS, "creator");
		Property dcdate = rdfResource.getModel().createProperty(DCTerms.NS, "date");
		List<String> anchors = new ArrayList<String>();
	    for (Resource reviewed: findReviewed()) {
                StringBuilder anchor = new StringBuilder();
                Resource creator = reviewed.getPropertyResourceValue(dccreator);
                if (creator != null) {
                    anchor.append(toHTML(creator.getRequiredProperty(FOAF.name).getLiteral()));
                    anchor.append(", ");
                }
                anchor.append("<em>");
                anchor.append(toHTML(reviewed.getRequiredProperty(dctitle).getLiteral()));
                anchor.append("</em> (");
                anchor.append(reviewed.getRequiredProperty(dcdate).getString().substring(0, 4));
                anchor.append(")");
                anchors.add(anchor.toString());
	    }
	    return StringUtils.join(anchors, "; ");
	}

    @Override
    protected String rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return true;
    }

    private List<Resource> findReviewed() {
        List<Resource> result = new ArrayList<Resource>();
        StmtIterator i = rdfResource.listProperties(MHS.reviews);
        if (!i.hasNext())
            throw new IllegalArgumentException("Review does not review anything");
        while (i.hasNext())
            result.add(i.nextStatement().getObject().as(Resource.class));
        return result;
    }

}
