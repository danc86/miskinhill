package au.com.miskinhill.search.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class OffsetTokenFilter extends TokenFilter {
	
	private int offset;

	protected OffsetTokenFilter(TokenStream input, int offset) {
		super(input);
		this.offset = offset;
	}
	
	@Override
	public Token next(Token reusableToken) throws IOException {
		Token retval = input.next(reusableToken);
		if (retval != null && offset != 0) {
			retval.setStartOffset(retval.startOffset() + offset);
			retval.setEndOffset(retval.endOffset() + offset);
		}
		return retval;
	}

}
