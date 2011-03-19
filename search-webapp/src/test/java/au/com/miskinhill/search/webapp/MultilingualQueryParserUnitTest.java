package au.com.miskinhill.search.webapp;

import static org.junit.Assert.*;

import java.io.Reader;

import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.AttributeSource;
import org.junit.Test;

import au.com.miskinhill.search.analysis.Analyzer;
import au.com.miskinhill.search.analysis.EnglishAnalyzer;
import au.com.miskinhill.search.analysis.PerLanguageAnalyzerMap;

public class MultilingualQueryParserUnitTest {
	
	private static final String[] fieldsToSearch = { "field1", "field2" };
	
	private static final Analyzer KEYWORD_ANALYZER = new Analyzer() {
        @Override
        public TokenStream tokenizer(AttributeSource attributeSource, Reader input) {
            return new KeywordTokenizer(attributeSource, input, 256);
        }
        @Override
        public TokenStream tokenizer(Reader input) {
            return new KeywordTokenizer(input);
        }
        @Override
        public TokenStream applyFilters(TokenStream input) {
            return input;
        }
    }; 
	
	@Test
	public void testParse() throws Exception {
		PerLanguageAnalyzerMap analyzerMap = new PerLanguageAnalyzerMap(KEYWORD_ANALYZER);
		analyzerMap.addAnalyzer("en", new EnglishAnalyzer());
		Query query = MultilingualQueryParser.parse("bob's a silly heads", analyzerMap, fieldsToSearch);
		assertEquals("+(field1:bob field1:bob's field2:bob field2:bob's) " + 
				"(field1:a field2:a) " + 
				"+(field1:silli field1:silly field2:silli field2:silly) " + 
				"+(field1:head field1:heads field2:head field2:heads)", 
				query.toString());
	}
	
	/**
	 * Tests parsing when a query token becomes two tokens in analysing.
	 */
	@Test
	public void testParsePhrase() throws Exception {
        PerLanguageAnalyzerMap analyzerMap = new PerLanguageAnalyzerMap(KEYWORD_ANALYZER);
        analyzerMap.addAnalyzer("en", new EnglishAnalyzer());
		Query query = MultilingualQueryParser.parse("cha-cha char", analyzerMap, fieldsToSearch);
		assertEquals("+(field1:cha-cha field1:\"cha cha\" field2:cha-cha field2:\"cha cha\") " + 
				"+(field1:char field2:char)", 
				query.toString());
	}

}
