package au.com.miskinhill.search.webapp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.xml.sax.InputSource;

import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class SearchResultsTemplateTest {
	
    @Test
    public void shouldContainSearchTerm() throws Exception {
        Document doc = renderTemplate("russian", new SearchResults());
        assertThat(xpath("//html:h2").selectSingleNode(doc).getText(),
                containsString("russian"));
    }
    
    @Test
    public void shouldNotContainResultTypesWhichHaveNoResults() throws Exception {
        Document doc = renderTemplate("russian", dummyResults());
        for (Node resultType : (List<Node>) xpath("//*[@class='result-type']").selectNodes(doc)) {
        	assertThat(resultType.getText(), not(equalTo(SearchResults.ResultType.Review.name())));
        }
    }

    @Test
    public void shouldWarnWhenNoResults() throws Exception {
        Document doc = renderTemplate("russian", new SearchResults());
        assertThat(xpath("//*[@class='no-results']").selectSingleNode(doc).getText(),
                equalTo("Your search returned no results."));
    }

    @Test
    public void shouldLinkToResultURLs() throws Exception {
        Document doc = renderTemplate("russian", dummyResults());
        List<String> hrefs = new ArrayList<String>();
        for (Element a: (List<Element>) xpath("//*[@class='searchresults']/html:tbody/html:tr/html:td/html:a").selectNodes(doc))
            hrefs.add(a.attributeValue("href"));
        assertThat(hrefs,
                hasItems("http://miskinhill.com.au/journals/asees/21:1-2/nabat-and-its-editors",
                         "http://miskinhill.com.au/journals/asees/21:1-2/moscow-street-names"));
    }

    @Test
    public void shouldShowRelevance() throws Exception {
        Document doc = renderTemplate("russian", dummyResults());
        List<Element> imgs = xpath("//*[@class='relevance-container']/html:img").selectNodes(doc);
        assertThat(imgs.get(0).attributeValue("alt"), equalTo("90.0%"));
        assertThat(imgs.get(0).attributeValue("title"), equalTo("90.0%"));
        assertThat(imgs.get(0).attributeValue("width"), equalTo("36"));
        assertThat(imgs.get(1).attributeValue("alt"), equalTo("50.0%"));
        assertThat(imgs.get(1).attributeValue("title"), equalTo("50.0%"));
        assertThat(imgs.get(1).attributeValue("width"), equalTo("20"));
        assertThat(imgs.get(2).attributeValue("alt"), equalTo("10.0%"));
        assertThat(imgs.get(2).attributeValue("title"), equalTo("10.0%"));
        assertThat(imgs.get(2).attributeValue("width"), equalTo("4"));
    }
	   
    private Document renderTemplate(String q, SearchResults results) throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("q", q);
        context.put("results", results);
        SimpleTemplateEngine engine = new SimpleTemplateEngine();
        Template template = engine.createTemplate(new InputStreamReader(
                this.getClass().getResourceAsStream("SearchResults.html"), "UTF-8"));
        StringWriter writer = new StringWriter();
        template.make(context).writeTo(writer);
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new XhtmlEntityResolver());
        return reader.read(new InputSource(new StringReader(writer.toString())));
    }

    private SearchResults dummyResults() {
        SearchResults results = new SearchResults();
        results.add(SearchResults.ResultType.Author,
                "http://miskinhill.com.au/authors/stern-l",
                "Ludmila Stern", 0.9f);
        results.add(SearchResults.ResultType.Article,
                "http://miskinhill.com.au/journals/asees/21:1-2/nabat-and-its-editors",
                "Nabat and its editors, &amp; Нас еще судьбы безвестные ждут", 0.5f);
        results.add(SearchResults.ResultType.Article,
                "http://miskinhill.com.au/journals/asees/21:1-2/moscow-street-names",
                "Moscow street names", 0.1f);
        return results;
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentFactory.getInstance().createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }
	
}
