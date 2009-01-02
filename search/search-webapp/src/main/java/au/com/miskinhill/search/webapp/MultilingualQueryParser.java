package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import au.com.miskinhill.search.analysis.PerLanguageAnalyzerWrapper;

public class MultilingualQueryParser {

	public static Query parse(String q, PerLanguageAnalyzerWrapper analyzer, 
			String[] fieldsToSearch) throws IOException {
		BooleanQuery query = new BooleanQuery();
		List<Analyzer> subAnalyzers = analyzer.getAnalyzers();
		for (String token: consumeTokens(new WhitespaceTokenizer(new StringReader(q)))) {
			BooleanQuery tokenQuery = new BooleanQuery();
			boolean tokenRequired = true;
			for (Analyzer subAnalyzer: subAnalyzers) {
				for (String field: fieldsToSearch) {
					List<String> analyzedTokens = consumeTokens(
							subAnalyzer.tokenStream(field, new StringReader(token)));
					switch (analyzedTokens.size()) {
						case 0:
							/*
							 * If any language returns no tokens, it means it's
							 * been stop-worded away. In this case we have to
							 * make the entire query for this token optional,
							 * otherwise our entire query will never match
							 * against languages where thisis a stop-word.
							 */
							tokenRequired = false;
							break;
						case 1:
							tokenQuery.add(new TermQuery(new Term(field, 
									analyzedTokens.get(0))), Occur.SHOULD);
							break;
						default:
							PhraseQuery phrase = new PhraseQuery();
							for (String analyzedToken: analyzedTokens)
								phrase.add(new Term(field, analyzedToken));
							tokenQuery.add(phrase, Occur.SHOULD);
					}
				}
			}
			query.add(tokenQuery, tokenRequired ? Occur.MUST : Occur.SHOULD);
		}
		return query;
	}
	
	private static List<String> consumeTokens(TokenStream stream) throws IOException {
		List<String> tokens = new ArrayList<String>();
		Token tok = new Token();
		while ((tok = stream.next(tok)) != null) {
			tokens.add(tok.term());
		}
		return tokens;
	}
	
}
