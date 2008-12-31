package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import au.com.miskinhill.search.analysis.MHAnalyzer;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1140261890520211317L;
	
	private static final String[] fieldsToSearch = {
		"http://purl.org/dc/terms/title", 
		"http://xmlns.com/foaf/0.1/name", 
		"http://miskinhill.com.au/rdfschema/1.0/biographicalNotes", 
		"content"
	};
	
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
		try {
			String q = req.getParameter("q");
			if (q == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing q argument");
				return;
			}
			
			IndexSearcher searcher = new IndexSearcher(index);
			Query query = MultilingualQueryParser.parse(q, new MHAnalyzer(), fieldsToSearch);
			TopDocs topDocs = searcher.search(query, 50);
			List<Result> results = new ArrayList<Result>();
			for (int i = 0; i < topDocs.totalHits; i ++) {
				Document doc = index.document(topDocs.scoreDocs[i].doc);
				Result result = new Result(doc.get("url"), topDocs.scoreDocs[i].score);
				results.add(result);
			}
			
			VelocityContext context = new VelocityContext();
			context.put("q", q);
			context.put("results", results);
			Template template = initVelocity().getTemplate(
					"/au/com/miskinhill/search/webapp/SearchResultsTemplate.vm");
			resp.setContentType("text/html; charset=utf-8");
			template.merge(context, resp.getWriter());
		} catch (Exception e) {
			// Java is lame
			throw new ServletException(e);
		}
	}
	
	// XXX lame
	private VelocityEngine initVelocity() throws Exception {
		Properties props = new Properties();
		props.load(SearchServlet.class.getResourceAsStream("/velocity.properties"));
		VelocityEngine ve = new VelocityEngine();
		ve.setApplicationAttribute(ServletContext.class.getName(), 
				getServletContext());
		ve.init(props);
		return ve;
	}
	
	public static class Result {
		private String url;
		private float score;
		public Result(String url, float score) {
			this.url = url;
			this.score = score;
		}
		public String getUrl() { return url; }
		public float getScore() { return score; }
	}

}