package au.com.miskinhill.search.webapp;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.search.Query;
import org.junit.Test;

import au.com.miskinhill.search.analysis.PerLanguageAnalyzerWrapper;

/**
 * In the same vein as {@link org.apache.lucene.queryParser.QueryParser}, parses
 * a free text query and returns a {@link Query} instance. Intended for search
 * which have been analysed with a {@link PerLanguageAnalyzerWrapper}.
 */
public class MultilingualQueryParserUnitTest {
	
	private static final String[] fieldsToSearch = { "field1", "field2" };
	
	@Test
	public void testParse() throws Exception {
		PerLanguageAnalyzerWrapper analyzer = new PerLanguageAnalyzerWrapper(new KeywordAnalyzer());
		analyzer.addAnalyzer("en", new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS));
		Query query = MultilingualQueryParser.parse("bob's a silly heads", analyzer, fieldsToSearch);
		assertEquals("+(field1:bob's field2:bob's field1:bob field2:bob) " + 
				"(field1:a field2:a) " + 
				"+(field1:silly field2:silly field1:silli field2:silli) " + 
				"+(field1:heads field2:heads field1:head field2:head)", 
				query.toString());
	}
	
	/**
	 * Tests parsing when a query token becomes two tokens in analysing.
	 */
	@Test
	public void testParsePhrase() throws Exception {
		PerLanguageAnalyzerWrapper analyzer = new PerLanguageAnalyzerWrapper(new KeywordAnalyzer());
		analyzer.addAnalyzer("en", new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS));
		Query query = MultilingualQueryParser.parse("cha-cha char", analyzer, fieldsToSearch);
		assertEquals("+(field1:cha-cha field2:cha-cha field1:\"cha cha\" field2:\"cha cha\") " + 
				"+(field1:char field2:char field1:char field2:char)", 
				query.toString());
	}

}
