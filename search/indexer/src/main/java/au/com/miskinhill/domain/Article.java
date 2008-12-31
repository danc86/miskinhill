package au.com.miskinhill.domain;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.SequenceInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import au.com.miskinhill.search.analysis.MHAnalyzer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;
import au.com.miskinhill.search.analysis.XMLTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Article extends GenericResource implements Indexable {
	
	private Document doc = new Document();
	
	private static final byte[] XHTML_STRICT_DTD_DECL = 
			("<!DOCTYPE html " +
			"PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
			"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
			.getBytes();
	
	public Article(Resource res) throws UnknownLiteralTypeException, FileNotFoundException, XMLStreamException {
		super(res);
		
		doc.add(new Field("url", res.getURI(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		
		StmtIterator i = res.listProperties();
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			stmt.getObject().visitWith(new RDFVisitor() {
				@Override
				public Object visitBlank(com.hp.hpl.jena.rdf.model.Resource r, AnonId id) {
					/* pass */
					return null;
				}

				@Override
				public Object visitLiteral(Literal literal) {
					try {
						doc.add(new Field(stmt.getPredicate().getURI(), 
								RDFLiteralTokenizer.fromLiteral(literal)));
					} catch (UnknownLiteralTypeException e) {
						throw new RuntimeException(e);
					}
					return null;
				}

				@Override
				public Object visitURI(com.hp.hpl.jena.rdf.model.Resource r, String uri) {
					return null;
				}
				
			});
		}
		
		assert res.getURI().substring(0, 25) == "http://miskinhill.com.au/";
		File content = new File("../../content/" + res.getURI().substring(25) + ".html");
		doc.add(new Field("content", new XMLTokenizer(
				new SequenceInputStream(
					new ByteArrayInputStream(XHTML_STRICT_DTD_DECL), 
					new BufferedInputStream(new FileInputStream(content))), 
				new MHAnalyzer())));
	}
	
	public void addToIndex(IndexWriter iw) throws IOException {
		iw.addDocument(doc);
	}

}
