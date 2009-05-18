package au.com.miskinhill.search.analysis;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.analysis.Token;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.search.analysis.IntegerLiteralTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;

public class IntegerLiteralTokenizerUnitTest extends AbstractTokenizerUnitTest {
	
	private Literal zero;
	
	@Before
	public void setUpMock() {
		zero = createMock(Literal.class);
		expect(zero.getString()).andReturn("0").anyTimes();
		expect(zero.getDatatypeURI()).andReturn(XSD.integer.getURI()).anyTimes();
		replay(zero);
	}
	
	@After
	public void verifyMock() {
		if (zero != null) verify(zero);
	}

	@Test
	public void testInteger() {
		IntegerLiteralTokenizer t = new IntegerLiteralTokenizer(zero);
		assertEquals(buildToken("0", 0, 1, "integer"), t.next(new Token()));
		assertEquals(null, t.next(new Token()));
	}
	
	@Test
	public void testLiteralTypeLookup() throws Exception {
		RDFLiteralTokenizer t = RDFLiteralTokenizer.fromLiteral(zero);
		assertTrue(t instanceof IntegerLiteralTokenizer);
	}
	
}
