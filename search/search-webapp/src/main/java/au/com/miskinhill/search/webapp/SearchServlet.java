package au.com.miskinhill.search.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1140261890520211317L;
	
	private static IndexReader index;
	private static QueryParser parser;
	static {
		try {
			index = IndexReader.open(FSDirectory.getDirectory("../index-data"), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		parser = new QueryParser("content", new StandardAnalyzer());
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String query = req.getParameter("q");
		if (query == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing q argument");
			return;
		}
		
		IndexSearcher searcher = new IndexSearcher(index);
		TopDocs results;
		try {
			results = searcher.search(parser.parse(query), 50);
		} catch (ParseException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Unable to parse query");
			return;
		}
		
		resp.setContentType("text/plain");
		for (int i = 0; i < results.totalHits; i++) {
			resp.getOutputStream().write(
					(results.scoreDocs[i].doc + ": "
							+ results.scoreDocs[i].score + "\n").getBytes());
		}
	}

}
