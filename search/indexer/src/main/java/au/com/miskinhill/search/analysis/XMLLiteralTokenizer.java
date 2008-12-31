package au.com.miskinhill.search.analysis;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.analysis.Token;

import com.hp.hpl.jena.rdf.model.Literal;

public class XMLLiteralTokenizer extends RDFLiteralTokenizer {
	
	private XMLTokenizer delegate;

	public XMLLiteralTokenizer(Literal literal) throws XMLStreamException {
		delegate = new XMLTokenizer(new StringReader(preprocess(literal.getString())), new MHAnalyzer());
	}
	
	public Token next(Token reusableToken) throws IOException {
		return delegate.next(reusableToken);
	}
	
}
