package au.com.miskinhill.search.tokenizer;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;

public abstract class RDFLiteralTokenizer extends TokenStream {

	public static class UnknownLiteralTypeException extends Exception {
		private static final long serialVersionUID = 3417574009217953585L;

		public UnknownLiteralTypeException(String type) {
			super("Could not resolve literal type <" + type
					+ "> to an RDFLiteralTokenizer subclass");
		}
	}

	private static Map<String, Class<? extends RDFLiteralTokenizer>> types = 
			new HashMap<String, Class<? extends RDFLiteralTokenizer>>();
	static {
		types.put(null /* no type means plain string */, StringLiteralTokenizer.class);
		types.put(XSD.integer.getURI(), IntegerLiteralTokenizer.class);
	}

	public static RDFLiteralTokenizer fromLiteral(Literal literal) 
			throws UnknownLiteralTypeException {
		Class<? extends RDFLiteralTokenizer> tokenizerClass = 
				types.get(literal.getDatatypeURI());
		if (tokenizerClass == null)
			throw new UnknownLiteralTypeException(literal.getDatatypeURI());
		try {
			return tokenizerClass.getConstructor(Literal.class).newInstance(literal);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}