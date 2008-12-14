package au.com.miskinhill.search;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Article implements Indexable {
	
	private Document doc = new Document();
	
	public Article(Resource res) throws UnknownLiteralTypeException {
		StmtIterator i = res.listProperties();
		while (i.hasNext()) {
			Statement stmt = i.nextStatement();
			RDFNode n = stmt.getObject();
			if (n.isLiteral()) {
				doc.add(new Field(stmt.getPredicate().getURI(), 
						RDFLiteralTokenizer.fromLiteral((Literal) n.as(Literal.class))));
			}
		}
	}
	
	public void addToIndex(IndexWriter iw) throws IOException {
		iw.addDocument(doc);
	}

}
