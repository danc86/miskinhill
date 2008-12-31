package au.com.miskinhill.search.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.search.analysis.MHAnalyzer;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1140261890520211317L;
	
	private static IndexReader index;
	static {
		try {
			index = IndexReader.open(FSDirectory.getDirectory("../index-data"), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String q = req.getParameter("q");
		if (q == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing q argument");
			return;
		}
		
		IndexSearcher searcher = new IndexSearcher(index);
		Query query = MultilingualQueryParser.parse(q, new MHAnalyzer());
		TopDocs results = searcher.search(query, 50);
		
		resp.setContentType("text/plain");
		for (int i = 0; i < results.totalHits; i++) {
			resp.getOutputStream().write(
					(results.scoreDocs[i].doc + ": "
							+ results.scoreDocs[i].score + "\n").getBytes());
		}
	}

}
