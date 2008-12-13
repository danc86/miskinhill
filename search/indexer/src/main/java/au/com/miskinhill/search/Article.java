package au.com.miskinhill.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import au.com.miskinhill.search.tokenizer.RDFLiteralTokenizer;
import au.com.miskinhill.search.tokenizer.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Article {
	
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
	
	public String toString() {
		return doc.toString();
	}

}
