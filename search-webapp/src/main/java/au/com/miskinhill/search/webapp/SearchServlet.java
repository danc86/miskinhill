package au.com.miskinhill.search.webapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.search.analysis.MHAnalyzer;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1140261890520211318L;
	
	private static String[] fieldsToSearch;

    private Template resultsTemplate;
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
            fieldsToSearch = determineFieldsToSearch();

            SimpleTemplateEngine engine = new SimpleTemplateEngine();
            InputStreamReader resultsTemplateReader = new InputStreamReader(
                this.getClass().getResourceAsStream("SearchResults.html"), "UTF-8");
            try {
                resultsTemplate = engine.createTemplate(resultsTemplateReader);
            } finally {
                resultsTemplateReader.close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
	}
    
    @SuppressWarnings("unchecked")
    private String[] determineFieldsToSearch() {
        ArrayList<String> interestingFields = new ArrayList<String>();
        for (String fieldName: (Collection<String>) index.getFieldNames(FieldOption.INDEXED)) {
            if (!fieldName.equals("url") && !fieldName.equals("type"))
                interestingFields.add(fieldName);
        }
        return interestingFields.toArray(new String[0]);
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

            Map<String, Object> context = new HashMap<String, Object>();
	        context.put("resultTypes", SearchResults.ResultType.values());
			context.put("q", q);
			context.put("results", results);
			resp.setContentType("text/html; charset=utf-8");
            resultsTemplate.make(context).writeTo(resp.getWriter());
		} catch (Exception e) {
			// Java is lame
			throw new ServletException(e);
		}
	}

}
