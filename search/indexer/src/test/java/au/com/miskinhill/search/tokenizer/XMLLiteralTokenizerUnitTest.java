package au.com.miskinhill.search.tokenizer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.analysis.Token;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDF;

@SuppressWarnings("deprecation")
public class XMLLiteralTokenizerUnitTest {
	
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
		assertEquals(new Token("nabat", 57, 62, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("it", 72, 75, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("editor", 76, 83, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("1919", 89, 93, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("swansong", 94, 102, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("brisban", 110, 118, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("russian", 119, 126, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("socialist", 127, 136, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("press", 137, 142, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("нас", 161, 164, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("ещ", 165, 168, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("судьб", 169, 175, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("безвестн", 176, 186, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("ждут", 187, 191, "<ALPHANUM>"), t.next(tok));
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
