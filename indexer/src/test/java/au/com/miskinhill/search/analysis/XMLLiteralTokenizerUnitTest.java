package au.com.miskinhill.search.analysis;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.analysis.Token;
import org.junit.Test;

import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;
import au.com.miskinhill.search.analysis.XMLLiteralTokenizer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDF;

public class XMLLiteralTokenizerUnitTest extends AbstractTokenizerUnitTest {
	
	@Test
	public void testSpan() throws Exception {
		final String span = "<span xmlns=\"http://www.w3.org/1999/xhtml\" " +
				"lang=\"en\"><em>Nabat</em> and its editors: the 1919 " + 
				"swansong of the Brisbane Russian socialist press, or " +
				"<em lang=\"ru\">Нас еще судьбы безвестные ждут</em></span>";
		Literal literal = createMock(Literal.class);
		expect(literal.getString()).andReturn(span).anyTimes();
		replay(literal);
		XMLLiteralTokenizer t = new XMLLiteralTokenizer(literal);
		Token tok = new Token();
		assertEquals(buildToken("nabat", 57, 62, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("it", 72, 75, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("editor", 76, 83, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("1919", 89, 93, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("swansong", 94, 102, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("brisban", 110, 118, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("russian", 119, 126, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("socialist", 127, 136, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("press", 137, 142, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("нас", 161, 164, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("nas", 161, 164, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("ещ", 165, 168, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("eshche", 165, 168, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("судьб", 169, 175, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("sud'by", 169, 175, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("безвестн", 176, 186, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("bezvestnye", 176, 186, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(buildToken("ждут", 187, 191, "<ALPHANUM>"), t.next(tok));
		assertEquals(buildToken("zhdut", 187, 191, "<ALPHANUM>", 0), t.next(tok));
		assertEquals(null, t.next(new Token()));
	}
	
	@Test
	public void testCurlyApostrophe() throws Exception {
		final String span = "<span xmlns=\"http://www.w3.org/1999/xhtml\" " +
				"lang=\"en\">everyone\u2019s silly</span>";
		Literal literal = createMock(Literal.class);
		expect(literal.getString()).andReturn(span).anyTimes();
		replay(literal);
		XMLLiteralTokenizer t = new XMLLiteralTokenizer(literal);
		Token tok = new Token();
		assertEquals(buildToken("everyon", 53, 63, "<APOSTROPHE>"), t.next(tok));
		assertEquals(buildToken("silli", 64, 69, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(new Token()));
	}
	
	@Test
	public void testLiteralTypeLookup() throws Exception {
		Literal text = createMock(Literal.class);
		expect(text.getString()).andReturn("<div>hi there</div>").anyTimes();
		expect(text.getDatatypeURI()).andReturn(RDF.getURI() + "XMLLiteral").anyTimes();
		replay(text);
		RDFLiteralTokenizer t = RDFLiteralTokenizer.fromLiteral(text);
		assertTrue(t instanceof XMLLiteralTokenizer);
	}

}
