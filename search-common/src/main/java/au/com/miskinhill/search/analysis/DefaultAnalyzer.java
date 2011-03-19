package au.com.miskinhill.search.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

public class DefaultAnalyzer implements Analyzer {

    @Override
    public TokenStream tokenizer(Reader input) {
        return new StandardTokenizer(Version.LUCENE_30,
                new PreprocFilterReader(input));
    }

    @Override
    public TokenStream tokenizer(AttributeSource attributeSource, Reader input) {
        return new StandardTokenizer(Version.LUCENE_30, attributeSource,
                new PreprocFilterReader(input));
    }

    @Override
    public TokenStream applyFilters(TokenStream input) {
        return new LowerCaseFilter(new StandardFilter(input));
    }

}
