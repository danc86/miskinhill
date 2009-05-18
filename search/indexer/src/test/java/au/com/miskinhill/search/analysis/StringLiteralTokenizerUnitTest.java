package au.com.miskinhill.search.analysis;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.analysis.Token;
import org.junit.Test;

import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;
import au.com.miskinhill.search.analysis.StringLiteralTokenizer;

import com.hp.hpl.jena.rdf.model.Literal;

public class StringLiteralTokenizerUnitTest extends AbstractTokenizerUnitTest {

	@Test
	public void testEnglish() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
		expect(text.getLanguage()).andReturn("en").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testEnglishWithLocaleThingy() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
		expect(text.getLanguage()).andReturn("en-AU").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testRussian() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("Нас и судьбы безвестные ждут").anyTimes();
		expect(text.getLanguage()).andReturn("ru").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("нас", 0, 3, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("nas", 0, 3, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("судьб", 6, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("sud'by", 6, 12, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("безвестн", 13, 23, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("bezvestnye", 13, 23, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("ждут", 24, 28, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("zhdut", 24, 28, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testNonexistentLanguage() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
		expect(text.getLanguage()).andReturn("xx").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("to", 6, 8, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("the", 9, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}
	
	@Test
	public void testEmptyLanguage() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("Hello to the World!").anyTimes();
		expect(text.getLanguage()).andReturn("").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("to", 6, 8, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("the", 9, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}
	
	@Test
	public void testCurlyApostrophe() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("everyone\u2019s silly").anyTimes();
		expect(text.getLanguage()).andReturn("en").anyTimes();
		replay(text);
		StringLiteralTokenizer t = new StringLiteralTokenizer(text);
		Token tok = new Token();
		assertEquals(buildToken("everyon", 0, 10, "<APOSTROPHE>"), t.next(tok));
		assertEquals(buildToken("silli", 11, 16, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}
	
	@Test
	public void testLiteralTypeLookup() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("0").anyTimes();
		expect(text.getLanguage()).andReturn("en").anyTimes();
		expect(text.getDatatypeURI()).andReturn(null).anyTimes();
		replay(text);
		RDFLiteralTokenizer t = RDFLiteralTokenizer.fromLiteral(text);
		assertTrue(t instanceof StringLiteralTokenizer);
	}
	
}
