package au.com.miskinhill.search.analysis;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Performs useful (language-independent) substitutions on the underlying
 * reader.
 * 
 * This can't be a TokenFilter because the substitutions affect the result of
 * tokenizing.
 */
// this FilterReader crap is utterly braindead
public class PreprocFilterReader extends FilterReader {

	public PreprocFilterReader(Reader in) {
		super(in);
	}
	
	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException("piss off");
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int retval = super.read(cbuf, off, len);
		preprocess(cbuf);
		return retval;
	}
	
	private void preprocess(char[] buffer) {
		for (int i = 0; i < buffer.length; i ++) {
			if (buffer[i] == '\u2019') buffer[i] = '\'';
		}
	}

}