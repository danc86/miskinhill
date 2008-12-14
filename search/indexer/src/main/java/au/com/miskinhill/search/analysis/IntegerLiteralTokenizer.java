package au.com.miskinhill.search.analysis;

import org.apache.lucene.analysis.Token;

import com.hp.hpl.jena.rdf.model.Literal;

public class IntegerLiteralTokenizer extends RDFLiteralTokenizer {
	
	private String value;
	private boolean exhausted = false;
	
	public IntegerLiteralTokenizer(Literal node) {
		value = node.getString();
	}
	
	public Token next(Token reusableToken) {
		if (exhausted)
			return null;
		exhausted = true;
		return reusableToken.reinit(value, 0, value.length(), "integer");
	}
	
}
