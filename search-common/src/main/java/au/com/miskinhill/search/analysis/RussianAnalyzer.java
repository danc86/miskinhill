package au.com.miskinhill.search.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;

/**
 * Like {@link SnowballAnalyzer}, but also filtered with
 * {@link CyrillicTransliteratingFilter}.
 */
public class RussianAnalyzer extends DefaultAnalyzer {
    
    private final static Set<String> STOP_WORDS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
            "\u0438" /* i */, 
            "\u0430" /* a */))); // XXX find more?

    @Override
    public TokenStream applyFilters(TokenStream input) {
        TokenStream result = new StandardFilter(input);
        result = new LowerCaseFilter(result);
        result = new StopFilter(true, result, STOP_WORDS);
        /* Transliteration happens before stemming, which
         * means the Latin version of each token will not be stemmed. That's
         * okay though, since the unstemmed version should always appear in
         * search queries due to the default analyzer. */
        result = new CyrillicTransliteratingFilter(result);
        result = new SnowballFilter(result, "Russian");
        return result;
    }

}
