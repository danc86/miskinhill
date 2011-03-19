package au.com.miskinhill.search.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;

public class EnglishAnalyzer extends DefaultAnalyzer {
    
    private final static Set<String> STOP_WORDS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
            // copied from StopAnalyzer.ENGLISH_STOP_WORDS
            "a", "an", "and", "but", "of", "or", "to", "the")));

    @Override
    public TokenStream applyFilters(TokenStream input) {
        return new SnowballFilter(new StopFilter(true,
                new LowerCaseFilter(new StandardFilter(input)),
                STOP_WORDS), "English");
    }

}
