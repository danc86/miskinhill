package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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

    private VelocityEngine ve;
	private IndexReader index;

    public static final String INDEX_PATH_PARAM = "au.com.miskinhill.search.indexPath";

    @Override
    public void init() throws ServletException {
        final String indexPath = getServletContext().getInitParameter(INDEX_PATH_PARAM);
        if (indexPath == null) {
            throw new ServletException("Parameter " + INDEX_PATH_PARAM + " not set");
        }
		try {
            // Lucene index
            index = IndexReader.open(FSDirectory.getDirectory(indexPath), /* read-only */ true);

            // Velocity
            Properties props = new Properties();
            props.load(SearchServlet.class.getResourceAsStream("/velocity.properties"));
            ve = new VelocityEngine();
            ve.setApplicationAttribute(ServletContext.class.getName(), 
                    getServletContext());
            ve.init(props);
        } catch (Exception e) {
            throw new ServletException(e);
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
			SearchResults results = SearchResults.build(searcher.search(query, 50), index);
			
			VelocityContext context = new VelocityContext();
	        context.put("resultTypes", SearchResults.ResultType.values());
			context.put("q", q);
			context.put("results", results);
			Template template = ve.getTemplate(
					"/au/com/miskinhill/search/webapp/SearchResultsTemplate.vm");
			resp.setContentType("text/html; charset=utf-8");
			template.merge(context, resp.getWriter());
		} catch (Exception e) {
			// Java is lame
			throw new ServletException(e);
		}
	}

}
