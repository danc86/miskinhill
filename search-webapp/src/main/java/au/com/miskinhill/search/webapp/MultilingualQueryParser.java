package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import au.com.miskinhill.search.analysis.Analyzer;
import au.com.miskinhill.search.analysis.PerLanguageAnalyzerMap;
import au.com.miskinhill.search.analysis.PreprocFilterReader;

/**
 * In the same vein as {@link org.apache.lucene.queryParser.QueryParser}, parses
 * a free text query and returns a {@link Query} instance. Intended for
 * searching indexes which have been analysed with a
 * {@link PerLanguageAnalyzerWrapper}.
 */
public class MultilingualQueryParser {

	public static Query parse(String q, PerLanguageAnalyzerMap analyzerMap, 
			String[] fieldsToSearch) throws IOException {
		BooleanQuery query = new BooleanQuery();
		List<Analyzer> subAnalyzers = analyzerMap.getAnalyzers();
		for (String token: consumeTokens(new WhitespaceTokenizer(new StringReader(q)))) {
		    /*
             * It is unlikely that any document would match more than one
             * component of this BooleanQuery, so we disable coord to avoid
             * skewing the scores drastically downward.
             */
			BooleanQuery tokenQuery = new BooleanQuery(/* disableCoord */ true);
			boolean tokenRequired = true;
            for (String field: fieldsToSearch) {
                /*
                 * Build a set first, because different analyzers might return
                 * the same tokens, e.g. "2006" analyses to the same thing for
                 * all languages.
                 */
                Set<List<String>> analyzedTokensSet = new HashSet<List<String>>();
                for (Analyzer subAnalyzer: subAnalyzers) {
                    TokenStream tokenStream = subAnalyzer.applyFilters(
                            subAnalyzer.tokenizer(
                                new PreprocFilterReader(new StringReader(token))));
                    analyzedTokensSet.add(consumeTokens(tokenStream));
                }
                for (List<String> analyzedTokens: analyzedTokensSet) {
					switch (analyzedTokens.size()) {
						case 0:
							/*
                             * If any analyzer returns nothing, it means this
                             * token has been stop-worded away in that language.
                             * In this case we have to make the query for this
                             * token optional, otherwise our entire query will
                             * never match against languages where this is a
                             * stop-word.
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
		while (stream.incrementToken()) {
		    tokens.add(stream.getAttribute(TermAttribute.class).term());
		}
		return tokens;
	}
	
}
