package au.com.miskinhill.search.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 * In the same vein as
 * {@link org.apache.lucene.analysis.PerFieldAnalyzerWrapper}, this analyzer
 * delegates to a sub-analyzer according to based on the language of the text
 * being analysed. The default sub-analyzer is given in the constructor; this is
 * used when the language is not specified, or when a language is specified for
 * which we have no specific sub-analyzer. Use
 * {@link #addAnalyzer(String, Analyzer)} to add a sub-analyzer for a specific
 * language.
 * <p>
 * Note that languages are matched by prefix, so that if a sub-analyzer has been
 * added for "en" (but not "en-AU"), it will be selected when analysing text
 * whose language is given as "en-AU".
 */
public class PerLanguageAnalyzerWrapper extends Analyzer {

	protected Trie<Analyzer> analyzers;
	
	public PerLanguageAnalyzerWrapper(Analyzer defaultAnalyzer) {
		analyzers = new Trie<Analyzer>(defaultAnalyzer);
	}
	
	public void addAnalyzer(String language, Analyzer analyzer) {
		analyzers.put(language, analyzer);
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		return tokenStream("", fieldName, reader);
	}
	
	public TokenStream tokenStream(String language, String fieldName, Reader reader) {
		Analyzer a = analyzers.get(language);
		return a.tokenStream(fieldName, reader);
	}

}
