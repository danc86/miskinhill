package au.com.miskinhill.search.analysis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class PreprocFilterReaderUnitTest {
	
	@Test
	public void testRemovesCurlyApostrophe() throws Exception {
		assertEquals("This ain't got no curly quotin' in it", 
				exhaust(new PreprocFilterReader(new StringReader(
						"This ain\u2019t got no curly quotin\u2019 in it"))));
	}
	
	private String exhaust(Reader reader) throws IOException {
		StringBuffer buff = new StringBuffer();
		int charsRead;
		char[] cb = new char[4096];
		while ((charsRead = reader.read(cb)) > 0) {
			buff.append(cb, 0, charsRead);
		}
		return buff.toString();
	}

}
