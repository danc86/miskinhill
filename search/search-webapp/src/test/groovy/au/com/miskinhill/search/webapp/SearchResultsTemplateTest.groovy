package au.com.miskinhill.search.webapp

import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.*
import static org.junit.matchers.JUnitMatchers.*
import org.junit.Test
import groovy.text.SimpleTemplateEngine
import groovy.util.XmlParser
import groovy.xml.Namespace

class SearchResultsTemplateTest {
	
    @Test
    void shouldContainSearchTerm() {
        def html = renderTemplate([q: "russian", results: new SearchResults()])
        assertThat html.body.div.h2.text(), containsString("russian")
    }
    
    @Test
    void shouldNotContainResultTypesWhichHaveNoResults() {
        def html = renderTemplate([q: "russian", results: dummyResults()])
        def resultTypes = html.depthFirst().grep{it."@class" == "result-type"}
        for (resultType in resultTypes) {
        	assertThat resultType.text(), not(equalTo(SearchResults.ResultType.Review as String))
        }
    }

    @Test
    void shouldWarnWhenNoResults() throws Exception {
        def html = renderTemplate([q: "russian", results: new SearchResults()])
        assertThat html.depthFirst().grep{it."@class" == "no-results"}[0].text(), equalTo("Your search returned no results.")
    }

    @Test
    void shouldLinkToResultURLs() throws Exception {
        def html = renderTemplate([q: "russian", results: dummyResults()])
        assertThat html.depthFirst().grep{it."@class" == "searchresults"}.tbody.tr.td.a."@href".flatten(),
                hasItems("http://miskinhill.com.au/journals/asees/21:1-2/nabat-and-its-editors",
                         "http://miskinhill.com.au/journals/asees/21:1-2/moscow-street-names")
    }

    @Test
    void shouldShowRelevance() throws Exception {
        def html = renderTemplate([q: "russian", results: dummyResults()])
        def imgs = html.depthFirst().grep{it."@class" == "relevance-container"}.img
        assertThat imgs[0]."@alt".text(), equalTo("90.0%")
        assertThat imgs[0]."@title".text(), equalTo("90.0%")
        assertThat imgs[0]."@width".text(), equalTo("36")
        assertThat imgs[1]."@alt".text(), equalTo("50.0%")
        assertThat imgs[1]."@title".text(), equalTo("50.0%")
        assertThat imgs[1]."@width".text(), equalTo("20")
        assertThat imgs[2]."@alt".text(), equalTo("10.0%")
        assertThat imgs[2]."@title".text(), equalTo("10.0%")
        assertThat imgs[2]."@width".text(), equalTo("4")
    }
	   
    def renderTemplate(Map<String, Object> context) {
        def engine = new SimpleTemplateEngine();
        def template = engine.createTemplate(new InputStreamReader(
                this.getClass().getResourceAsStream("SearchResults.html"), "UTF-8"));
        def writer = new StringWriter()
        template.make(context).writeTo(writer)
        return new XmlParser().parseText(writer.toString())
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
	
}
