package au.com.miskinhill.search.analysis;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

public class CyrillicTransliteratingFilterUnitTest {

    @Test
    public void shouldPassOnTokensWithoutCyrillicUntouched() throws IOException {
        Token asdf = new Token();
        asdf.setTermBuffer("asdf");
        asdf.setStartOffset(1);
        asdf.setEndOffset(4);
        assertThat(filter(Arrays.asList(asdf)),
                equalTo(Arrays.asList(asdf)));
    }
    
    @Test
    public void shouldTransliterateCyrillicTokens() throws IOException {
        Token igraCyrillic = new Token();
        igraCyrillic.setTermBuffer("игра");
        igraCyrillic.setStartOffset(1);
        igraCyrillic.setEndOffset(4);
        Token igraLatin = new Token();
        igraLatin.setTermBuffer("igra");
        igraLatin.setStartOffset(1);
        igraLatin.setEndOffset(4);
        igraLatin.setPositionIncrement(0);
        assertThat(filter(Arrays.asList(igraCyrillic)),
                equalTo(Arrays.asList(igraCyrillic, igraLatin)));
    }
    
    @Test
    public void shouldTransliterateTokensWithMixedLatinAndCyrillic() throws IOException {
        Token mixed = new Token();
        mixed.setTermBuffer("interнет");
        mixed.setStartOffset(1);
        mixed.setEndOffset(4);
        Token latin = new Token();
        latin.setTermBuffer("internet");
        latin.setStartOffset(1);
        latin.setEndOffset(4);
        latin.setPositionIncrement(0);
        assertThat(filter(Arrays.asList(mixed)),
                equalTo(Arrays.asList(mixed, latin)));
    }
    
    private List<Token> filter(List<Token> input) throws IOException {
        final Iterator<Token> inputIt = input.iterator();
        TokenStream inputStream = new TokenStream() {
            @Override
            public Token next(Token reusableToken) throws IOException {
                if (!inputIt.hasNext()) return null;
                else return inputIt.next();
            }
        };
        CyrillicTransliteratingFilter filter = new CyrillicTransliteratingFilter(inputStream);
        List<Token> output = new ArrayList<Token>();
        while (true) {
            Token next = filter.next(new Token());
            if (next == null) break;
            output.add(next);
        }
        return output;
    }

}
