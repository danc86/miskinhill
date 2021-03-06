package au.com.miskinhill.search.webapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import au.com.miskinhill.search.analysis.MHAnalyzers;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1140261890520211318L;
	
	private static String[] fieldsToSearch;

    private Template resultsTemplate;
	private IndexReader index;

    public static final String INDEX_PATH_PROPERTY = "au.com.miskinhill.indexPath";

    @Override
    public void init() throws ServletException {
        final String indexPath = System.getProperty(INDEX_PATH_PROPERTY);
        if (indexPath == null) {
            throw new ServletException("System property " + INDEX_PATH_PROPERTY + " not set");
        }
		try {
            // Lucene index
            index = IndexReader.open(FSDirectory.open(new File(indexPath)), /* read-only */ true);
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
    
    private String[] determineFieldsToSearch() {
        ArrayList<String> interestingFields = new ArrayList<String>();
        for (String fieldName: index.getFieldNames(FieldOption.INDEXED)) {
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
			Query query = MultilingualQueryParser.parse(q, MHAnalyzers.getAnalyzerMap(), fieldsToSearch);
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
