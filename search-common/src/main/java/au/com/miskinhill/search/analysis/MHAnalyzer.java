package au.com.miskinhill.search.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class MHAnalyzer extends PerLanguageAnalyzerWrapper {

	public MHAnalyzer() {
		super(new StandardAnalyzer(new String[0] /* no stop words */));
		addAnalyzer("en", new SnowballAnalyzer("English", ENGLISH_STOP_WORDS));
		addAnalyzer("ru", new RussianAnalyzer());
		addAnalyzer("de", new SnowballAnalyzer("German", GERMAN_STOP_WORDS));
		addAnalyzer("fr", new SnowballAnalyzer("French", FRENCH_STOP_WORDS));
	}
	
	private final static String[] ENGLISH_STOP_WORDS = {
			// copied from StopAnalyzer.ENGLISH_STOP_WORDS
			"a", "an", "and", "but", "of", "or", "to", "the" };
	private final static String[] GERMAN_STOP_WORDS = {
	        // stop words are applied before snowballification
	        "der", "den", "des", "dem", "die", "das", 
	        "ein", "einen", "eines", "einem", "eine", "einer",
	        "oder", "und" };
	private final static String[] FRENCH_STOP_WORDS = {
	        "la", "le", "de", "des", "du", "au", "aux", "un", "une" };
	
	@Override
	public TokenStream tokenStream(String language, String fieldName,
			Reader reader) {
		return super.tokenStream(language, fieldName, new PreprocFilterReader(reader));
	}

}
