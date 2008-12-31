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
	
	@Test
	public void testParse() throws Exception {
		PerLanguageAnalyzerWrapper analyzer = new PerLanguageAnalyzerWrapper(new KeywordAnalyzer());
		analyzer.addAnalyzer("en", new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS));
		Query query = MultilingualQueryParser.parse("bob's a silly heads", analyzer);
		assertEquals("+(http://purl.org/dc/terms/title:bob's content:bob's http://purl.org/dc/terms/title:bob content:bob) " + 
				"+(http://purl.org/dc/terms/title:a content:a) " + 
				"+(http://purl.org/dc/terms/title:silly content:silly http://purl.org/dc/terms/title:silli content:silli) " + 
				"+(http://purl.org/dc/terms/title:heads content:heads http://purl.org/dc/terms/title:head content:head)", 
				query.toString());
	}
	
	/**
	 * Tests parsing when a query token becomes two tokens in analysing.
	 */
	@Test
	public void testParsePhrase() throws Exception {
		PerLanguageAnalyzerWrapper analyzer = new PerLanguageAnalyzerWrapper(new KeywordAnalyzer());
		analyzer.addAnalyzer("en", new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS));
		Query query = MultilingualQueryParser.parse("cha-cha char", analyzer);
		assertEquals("+(http://purl.org/dc/terms/title:cha-cha content:cha-cha http://purl.org/dc/terms/title:\"cha cha\" content:\"cha cha\") " + 
				"+(http://purl.org/dc/terms/title:char content:char http://purl.org/dc/terms/title:char content:char)", 
				query.toString());
	}

}
