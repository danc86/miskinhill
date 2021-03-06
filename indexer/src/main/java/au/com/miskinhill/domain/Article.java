package au.com.miskinhill.domain;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.search.analysis.MHAnalyzers;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;
import au.com.miskinhill.search.analysis.XMLTokenizer;

public class Article extends GenericResource {
    
    public static final String TYPE = MHS.NS_URI + "Article";
	
	public Article(Resource rdfResource, FulltextFetcher fulltextFetcher) {
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
		
		if (!rdfResource.getURI().startsWith("http://miskinhill.com.au/journals/"))
			throw new IllegalArgumentException("Cannot fetch content which is not under http://miskinhill.com.au/journals/");
        InputStream content = null;
        try {
            content = fulltextFetcher.fetchFulltext(rdfResource.getURI().substring(24) + ".html");
        } catch (FileNotFoundException e) {
            System.err.println("WARNING: failed to index content: " + e.getMessage());
        }
        if (content != null) {
            doc.add(new Field(fieldNamePrefix + "content", new XMLTokenizer(
                    new SequenceInputStream(
                            new ByteArrayInputStream(XHTML_STRICT_DTD_DECL),
                            content),
                    MHAnalyzers.getAnalyzerMap())));
        }
	}
	
	@Override
	protected String getAnchorText() {
		Property dctitle = rdfResource.getModel().createProperty(DCTerms.NS + "title");
        return toHTML(rdfResource.getRequiredProperty(dctitle).getLiteral());
	}

    @Override
    protected String rdfType() {
        return TYPE;
    }

    @Override
    public boolean isTopLevel() {
        return rdfResource.getURI().startsWith("http://miskinhill.com.au/journals/");
    }

}
