package au.com.miskinhill.search.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import com.hp.hpl.jena.rdf.model.Literal;

public class StringLiteralTokenizer extends RDFLiteralTokenizer {
	
	private TokenStream delegate;

	public StringLiteralTokenizer(Literal node) {
		delegate = new StringTokenizer(node.getString(), node.getLanguage());
	}
	
	public Token next(Token reuseableToken) throws IOException {
		return delegate.next(reuseableToken);
	}
	
}
