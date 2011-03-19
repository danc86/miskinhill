package au.com.miskinhill.search.analysis;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Literal;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class IntegerLiteralTokenStream extends TokenStream {
	
    private final TermAttribute termAttribute;
    private final OffsetAttribute offsetAttribute;
    private final TypeAttribute typeAttribute;
	private final String value;
	private boolean exhausted = false;
	
	public IntegerLiteralTokenStream(Literal node) {
		value = node.getString();
		termAttribute = addAttribute(TermAttribute.class);
		offsetAttribute = addAttribute(OffsetAttribute.class);
		addAttribute(PositionIncrementAttribute.class);
		typeAttribute = addAttribute(TypeAttribute.class);
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		if (exhausted)
			return false;
		exhausted = true;
		clearAttributes();
		termAttribute.setTermBuffer(value);
		offsetAttribute.setOffset(0, value.length());
		typeAttribute.setType("integer");
		return true;
	}
	
}
