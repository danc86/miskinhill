package au.com.miskinhill.search.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

final class StringTokenizer extends TokenStream {
	
	private final static String[] RUSSIAN_STOP_WORDS = { "и", "а" }; // XXX find more?
	private final static String[] ENGLISH_STOP_WORDS = {
			// copied from StopAnalyzer.ENGLISH_STOP_WORDS
			"a", "an", "and", "but", "of", "or", "to", "the" };

	private static Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
	static {
		analyzers.put(null, new StandardAnalyzer(new String[0] /* no stop words */));
		analyzers.put("en", new SnowballAnalyzer("English", ENGLISH_STOP_WORDS));
		analyzers.put("ru", new SnowballAnalyzer("Russian", RUSSIAN_STOP_WORDS));
	}
	
	private TokenStream delegate;
	private int offset;
	
	public StringTokenizer(String text, String lang) {
		this(text, lang, 0);
	}

	public StringTokenizer(String text, String lang, int offset) {
		if (lang != null ? lang.length() > 2 : false)
			lang = lang.substring(0, 2);
		Analyzer analyzer = analyzers.get(lang);
		if (analyzer == null) {
			System.err.println("WARNING: no analyzer for language " + lang + ", using default");
			analyzer = analyzers.get(null); // use default
		}
		delegate = analyzer.tokenStream(null, new StringReader(text));
		this.offset = offset;
	}
	
	public Token next(Token reuseableToken) throws IOException {
		Token retval = delegate.next(reuseableToken);
		if (retval != null && offset != 0) {
			retval.setStartOffset(retval.startOffset() + offset);
			retval.setEndOffset(retval.endOffset() + offset);
		}
		return retval;
	}
	
}
