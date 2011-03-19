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

public class GermanAnalyzer extends DefaultAnalyzer {
    
    private final static Set<String> STOP_WORDS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
            // stop words are applied before snowballification
            "der", "den", "des", "dem", "die", "das", 
            "ein", "einen", "eines", "einem", "eine", "einer",
            "oder", "und")));

    @Override
    public TokenStream applyFilters(TokenStream input) {
        return new SnowballFilter(new StopFilter(true,
                new LowerCaseFilter(new StandardFilter(input)),
                STOP_WORDS), "German");
    }

}
