package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.TopDocs;

public class SearchResults {
    
    public static class Result {
    	private final String url;
    	private final String anchor;
    	private final float score;
    	public Result(String url, String anchor, float score) {
    		this.url = url;
    		this.anchor = anchor;
    		this.score = score;
    	}
    	public String getUrl() { return url; }
        public String getAnchor() { return anchor; }
        public int scoreWidth(int max) {
    		return (int) (Math.min(1.0, this.score) * max);
    	}
    	public String scorePercent() {
    		return String.format("%.1f%%", Math.min(1.0, this.score) * 100);
    	}
    }
    
    public static enum ResultType {
        Author, Article, unknown
    }

    public static SearchResults build(TopDocs topDocs, IndexReader index) throws IOException {
        SearchResults results = new SearchResults();
        for (int i = 0; i < topDocs.totalHits; i ++) {
            Document doc = index.document(topDocs.scoreDocs[i].doc);
            ResultType type;
            String typeUrl = doc.get("type");
            if (typeUrl == null || !typeUrl.startsWith("http://miskinhill.com.au/rdfschema/1.0/"))
                type = ResultType.unknown;
            else {
                try {
                    type = ResultType.valueOf(typeUrl.substring(39));
                } catch (IllegalArgumentException e) {
                    type = ResultType.unknown;
                }
            }
            results.add(type,
                    doc.get("url"),
                    doc.get("anchor"), 
                    topDocs.scoreDocs[i].score);
        }
        return results;
    }
    
    private final Map<ResultType, List<Result>> results;
    
    private SearchResults() {
        results = new HashMap<ResultType, List<Result>>();
        for (ResultType type: ResultType.values()) {
            results.put(type, new ArrayList<Result>());
        }
    }
    
    private void add(ResultType type, String url, String anchor, float score) {
        results.get(type).add(new Result(url, anchor, score));
    }

    public Iterable<Result> get(ResultType type) {
        return results.get(type);
    }

}
