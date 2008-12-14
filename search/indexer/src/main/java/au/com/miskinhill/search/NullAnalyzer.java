package au.com.miskinhill.search;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class NullAnalyzer extends Analyzer {
	
	public static final NullAnalyzer INSTANCE = new NullAnalyzer(); 

	public TokenStream tokenStream(String fieldName, Reader reader) {
		throw new RuntimeException("Attempt to analyze field " + fieldName + " with NullAnalyzer");
	}
	
}
