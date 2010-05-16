package au.com.miskinhill.search.analysis;

import org.apache.lucene.analysis.Token;

public abstract class AbstractTokenizerUnitTest {

    protected static Token buildToken(String text, int start, int end,
            String type, int posIncr) {
        Token tok = new Token();
        tok.setTermBuffer(text);
        tok.setStartOffset(start);
        tok.setEndOffset(end);
        tok.setType(type);
        tok.setPositionIncrement(posIncr);
        return tok;
    }

    protected static Token buildToken(String text, int start, int end,
            String type) {
        return buildToken(text, start, end, type, 1);
    }

}