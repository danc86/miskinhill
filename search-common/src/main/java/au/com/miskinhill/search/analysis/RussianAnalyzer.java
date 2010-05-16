package au.com.miskinhill.search.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * Like {@link SnowballAnalyzer}, but also filtered with
 * {@link CyrillicTransliteratingFilter}.
 */
public class RussianAnalyzer extends Analyzer {
    
    private final static String[] RUSSIAN_STOP_WORDS = { 
        "\u0438" /* i */, 
        "\u0430" /* a */ }; // XXX find more?

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new StandardTokenizer(reader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, RUSSIAN_STOP_WORDS);
        /* Transliteration happens before stemming, which
         * means the Latin version of each token will not be stemmed. That's
         * okay though, since the unstemmed version should always appear in
         * search queries due to the default analyzer. */
        result = new CyrillicTransliteratingFilter(result);
        result = new SnowballFilter(result, "Russian");
        return result;
    }

}
