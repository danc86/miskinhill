package au.com.miskinhill.search.analysis;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

public class RDFLiteralTokenizerUnitTest {
    
    private void assertNextToken(TokenStream tokenStream, String term,
            int start, int end, int posInc, String type) throws IOException {
        assertTrue(tokenStream.incrementToken());
        assertThat(tokenStream.getAttribute(TermAttribute.class).term(),
                equalTo(term));
        assertThat(tokenStream.getAttribute(OffsetAttribute.class).startOffset(),
                equalTo(start));
        assertThat(tokenStream.getAttribute(OffsetAttribute.class).endOffset(),
                equalTo(end));
        assertThat(tokenStream.getAttribute(PositionIncrementAttribute.class)
                .getPositionIncrement(), equalTo(posInc));
        assertThat(tokenStream.getAttribute(TypeAttribute.class).type(),
                equalTo(type));
    }
    
    @Test
    public void test_plain_literal_in_english() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("Hello to the World!").anyTimes();
        expect(text.getLanguage()).andReturn("en").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "hello", 0, 5, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "world", 13, 18, 3, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }

    @Test
    public void test_plain_literal_in_english_with_country() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("Hello to the World!").anyTimes();
        expect(text.getLanguage()).andReturn("en-AU").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "hello", 0, 5, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "world", 13, 18, 3, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }

    @Test
    public void test_plain_literal_in_russian() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("Нас и судьбы безвестные ждут").anyTimes();
        expect(text.getLanguage()).andReturn("ru").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "нас", 0, 3, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "nas", 0, 3, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "судьб", 6, 12, 2, "<ALPHANUM>");
        assertNextToken(tokenStream, "sud'by", 6, 12, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "безвестн", 13, 23, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "bezvestnye", 13, 23, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "ждут", 24, 28, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "zhdut", 24, 28, 0, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }

    @Test
    public void test_plain_literal_in_nonexistent_language() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("Hello to the World!").anyTimes();
        expect(text.getLanguage()).andReturn("xx").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "hello", 0, 5, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "to", 6, 8, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "the", 9, 12, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "world", 13, 18, 1, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }
    
    @Test
    public void test_plain_literal_with_empty_language() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("Hello to the World!").anyTimes();
        expect(text.getLanguage()).andReturn("").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "hello", 0, 5, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "to", 6, 8, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "the", 9, 12, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "world", 13, 18, 1, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }
    
    @Test
    public void test_plain_literal_with_curly_apostrophe() throws Exception {
        Literal text = createMock(Literal.class);
        expect(text.getString()).andReturn("everyone\u2019s silly").anyTimes();
        expect(text.getLanguage()).andReturn("en").anyTimes();
        expect(text.getDatatypeURI()).andReturn(null).anyTimes();
        replay(text);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(text);
        assertNextToken(tokenStream, "everyon", 0, 10, 1, "<APOSTROPHE>");
        assertNextToken(tokenStream, "silli", 11, 16, 1, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }

    @Test
    public void test_integer_literal() throws Exception {
        Literal zero = createMock(Literal.class);
        expect(zero.getString()).andReturn("0").anyTimes();
        expect(zero.getDatatypeURI()).andReturn(XSD.integer.getURI()).anyTimes();
        replay(zero);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(zero);
        assertNextToken(tokenStream, "0", 0, 1, 1, "integer");
        assertFalse(tokenStream.incrementToken());
    }
    
    @Test
    public void test_xml_literal() throws Exception {
        final String span = "<span xmlns=\"http://www.w3.org/1999/xhtml\" " +
                "lang=\"en\"><em>Nabat</em> and its editors: the 1919 " + 
                "swansong of the Brisbane Russian socialist press, or " +
                "<em lang=\"ru\">Нас еще судьбы безвестные ждут</em></span>";
        Literal literal = createMock(Literal.class);
        expect(literal.getString()).andReturn(span).anyTimes();
        expect(literal.getDatatypeURI()).andReturn(RDF.getURI() + "XMLLiteral").anyTimes();
        replay(literal);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(literal);
        assertNextToken(tokenStream, "nabat", 57, 62, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "it", 72, 75, 2, "<ALPHANUM>");
        assertNextToken(tokenStream, "editor", 76, 83, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "1919", 89, 93, 2, "<ALPHANUM>");
        assertNextToken(tokenStream, "swansong", 94, 102, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "brisban", 110, 118, 3, "<ALPHANUM>");
        assertNextToken(tokenStream, "russian", 119, 126, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "socialist", 127, 136, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "press", 137, 142, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "нас", 161, 164, 1 /* XXX should really be 2 */, "<ALPHANUM>");
        assertNextToken(tokenStream, "nas", 161, 164, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "ещ", 165, 168, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "eshche", 165, 168, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "судьб", 169, 175, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "sud'by", 169, 175, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "безвестн", 176, 186, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "bezvestnye", 176, 186, 0, "<ALPHANUM>");
        assertNextToken(tokenStream, "ждут", 187, 191, 1, "<ALPHANUM>");
        assertNextToken(tokenStream, "zhdut", 187, 191, 0, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }
    
    @Test
    public void test_xml_literal_with_curly_apostrophe() throws Exception {
        final String span = "<span xmlns=\"http://www.w3.org/1999/xhtml\" " +
                "lang=\"en\">everyone\u2019s silly</span>";
        Literal literal = createMock(Literal.class);
        expect(literal.getString()).andReturn(span).anyTimes();
        expect(literal.getDatatypeURI()).andReturn(RDF.getURI() + "XMLLiteral").anyTimes();
        replay(literal);
        TokenStream tokenStream = RDFLiteralTokenizer.fromLiteral(literal);
        assertNextToken(tokenStream, "everyon", 53, 63, 1, "<APOSTROPHE>");
        assertNextToken(tokenStream, "silli", 64, 69, 1, "<ALPHANUM>");
        assertFalse(tokenStream.incrementToken());
    }

}