package au.com.miskinhill.search.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class MHAnalyzer extends PerLanguageAnalyzerWrapper {

	public MHAnalyzer() {
		super(new StandardAnalyzer(new String[0] /* no stop words */));
		addAnalyzer("en", new SnowballAnalyzer("English", ENGLISH_STOP_WORDS));
		addAnalyzer("ru", new SnowballAnalyzer("Russian", RUSSIAN_STOP_WORDS));
	}
	
	private final static String[] RUSSIAN_STOP_WORDS = { 
            "\u0438" /* i */, 
            "\u0430" /* a */ }; // XXX find more?
	private final static String[] ENGLISH_STOP_WORDS = {
			// copied from StopAnalyzer.ENGLISH_STOP_WORDS
			"a", "an", "and", "but", "of", "or", "to", "the" };

}
