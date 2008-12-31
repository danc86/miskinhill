package au.com.miskinhill.domain;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import au.com.miskinhill.search.analysis.MHAnalyzer;
import au.com.miskinhill.search.analysis.XMLTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.Resource;

public class Article extends GenericResource implements Indexable {
	
	public Article(Resource rdfResource) {
		super(rdfResource);
	}

	private static final byte[] XHTML_STRICT_DTD_DECL = 
			("<!DOCTYPE html " +
			"PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
			"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
			.getBytes();
	
	@Override
	public void addFieldsToDocument(Document doc)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		super.addFieldsToDocument(doc);
		
		assert rdfResource.getURI().substring(0, 25) == "http://miskinhill.com.au/";
		File content = new File("../../content/" + rdfResource.getURI().substring(25) + ".html");
		doc.add(new Field("content", new XMLTokenizer(
				new SequenceInputStream(
					new ByteArrayInputStream(XHTML_STRICT_DTD_DECL), 
					new BufferedInputStream(new FileInputStream(content))), 
				new MHAnalyzer())));
	}

}
