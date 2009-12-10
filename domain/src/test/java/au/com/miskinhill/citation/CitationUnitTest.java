package au.com.miskinhill.citation;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.junit.Test;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class CitationUnitTest {
    
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    static {
        inputFactory.setXMLResolver(new XhtmlEntityResolver());
    }
    
    @Test
    public void fromDocumentShouldFindAllCitations() throws Exception {
        List<Citation> citations = citationsFromDocument();
        assertThat(citations.size(), equalTo(7));
    }
    
    @Test
    public void toRDFShouldWork() throws Exception {
        Set<Statement> citationStmts = citationsFromDocument().get(2).toRDF();
        
        assertThat(citationStmts.size(), equalTo(3));
        Resource articleRes = ResourceFactory.createResource("http://miskinhill.com.au/journals/test/1:1/test-article");
        Resource citationRes = ResourceFactory.createResource("http://miskinhill.com.au/journals/test/1:1/test-article#citation-3");
        Resource citedRes = ResourceFactory.createResource("http://miskinhill.com.au/cited/books/olcott-2001");
        assertThat(citationStmts, hasItems(
                ResourceFactory.createStatement(citationRes, RDF.type, MHS.Citation),
                ResourceFactory.createStatement(citationRes, DCTerms.isPartOf, articleRes),
                ResourceFactory.createStatement(citationRes, MHS.cites, citedRes)));
    }

    private List<Citation> citationsFromDocument() throws XMLStreamException {
        XMLEventReader reader = inputFactory.createXMLEventReader(this.getClass().getResourceAsStream("citations.xml"));
        return Citation.fromDocument(URI.create("http://miskinhill.com.au/journals/test/1:1/test-article"), reader);
    }
    
    @Test
    public void normalizeSpaceShouldReplaceNbsp() {
        assertThat(Citation.normalizeSpace("a\u00a0b"), equalTo("a b"));
    }
    
    @Test
    public void normalizeSpaceShouldReplaceNewline() {
        assertThat(Citation.normalizeSpace("a \n b"), equalTo("a b"));
    }
    
    @Test
    public void normalizeSpaceShouldNormalizeEdges() {
        assertThat(Citation.normalizeSpace("\ta \n\tb "), equalTo(" a b "));
    }
    
    private XMLEventReader x(String xml) throws XMLStreamException {
        return inputFactory.createXMLEventReader(new StringReader(xml));
    }
    
    @Test
    public void testBookfromDocument() throws Exception {
        XMLEventReader reader = x("<span xmlns=\"http://www.w3.org/1999/xhtml\" class=\"citation book\">" +
                "<span class=\"au\">Charles Vinicombe Penrose</span>, \n" + 
                "<em class=\"btitle\">A Memoir of James Trevenen</em>, edited by \n" + 
                "<span class=\"au\">Christopher Lloyd</span> and \n" + 
                "<span class=\"au\">R.&#160;C. Anderson</span>, (<span class=\"place\">London</span>: \n" + 
                "<span class=\"pub\">Navy Records Society</span>, <span class=\"date\">1959</span>), \n" + 
                "<span class=\"spage\">90</span>–<span class=\"epage\">91</span>" +
                "<span class=\"cites\" title=\"books/penrose-1959\" /></span>");
        Citation citation = Citation.fromDocument(URI.create("http://example.com/article"), reader).get(0);
        assertThat(citation.getCites(), equalTo(
                Collections.singleton(URI.create("http://miskinhill.com.au/cited/books/penrose-1959"))));
        assertThat(citation.getGenre(), equalTo(Genre.book));
        assertThat(citation.getOpenurlField("au"), equalTo(Arrays.asList(
                "Charles Vinicombe Penrose", "Christopher Lloyd", "R. C. Anderson")));
        assertThat(citation.getOpenurlField("btitle"), equalTo(Arrays.asList("A Memoir of James Trevenen")));
        assertThat(citation.getOpenurlField("place"), equalTo(Arrays.asList("London")));
        assertThat(citation.getOpenurlField("pub"), equalTo(Arrays.asList("Navy Records Society")));
        assertThat(citation.getOpenurlField("date"), equalTo(Arrays.asList("1959")));
        assertThat(citation.getOpenurlField("spage"), equalTo(Arrays.asList("90")));
        assertThat(citation.getOpenurlField("epage"), equalTo(Arrays.asList("91")));
    }
    
    @Test
    public void testBookitemfromDocument() throws Exception {
        XMLEventReader reader = x("<span xmlns=\"http://www.w3.org/1999/xhtml\" class=\"citation bookitem\">" +
                "<span class=\"au\" title=\"Lydia \n" + 
                "Black\">Black</span><span class=\"atitle\" title=\"“The Russians were \n" + 
                "Coming…”\" /><span class=\"au\" title=\"Robin Inglis\" /><span class=\"btitle\" \n" + 
                "title=\"Spain and the North Pacific Coast\" /><span class=\"place\" \n" + 
                "title=\"Vancouver\" /><span class=\"pub\" title=\"Maritime Museum Society\" \n" + 
                "/><span class=\"date\" title=\"1992\" />, \n" + 
                "<span class=\"spage\">31</span>–<span class=\"epage\">29</span>" +
                "<span class=\"cites\" title=\"books/black-1992\" /></span>");
        Citation citation = Citation.fromDocument(URI.create("http://example.com/article"), reader).get(0);
        assertThat(citation.getCites(), equalTo(
                Collections.singleton(URI.create("http://miskinhill.com.au/cited/books/black-1992"))));
        assertThat(citation.getGenre(), equalTo(Genre.bookitem));
        assertThat(citation.getOpenurlField("au"), equalTo(Arrays.asList("Lydia Black", "Robin Inglis")));
        assertThat(citation.getOpenurlField("atitle"), equalTo(Arrays.asList("“The Russians were Coming…”")));
        assertThat(citation.getOpenurlField("btitle"), equalTo(Arrays.asList("Spain and the North Pacific Coast")));
        assertThat(citation.getOpenurlField("place"), equalTo(Arrays.asList("Vancouver")));
        assertThat(citation.getOpenurlField("pub"), equalTo(Arrays.asList("Maritime Museum Society")));
        assertThat(citation.getOpenurlField("date"), equalTo(Arrays.asList("1992")));
        assertThat(citation.getOpenurlField("spage"), equalTo(Arrays.asList("31")));
        assertThat(citation.getOpenurlField("epage"), equalTo(Arrays.asList("29"))); // sic
    }
    
    @Test
    public void testThesisfromDocument() throws Exception {
        XMLEventReader reader = x("<span xmlns=\"http://www.w3.org/1999/xhtml\" class=\"citation thesis\">" +
                "<span class=\"au\">Anthony H. \n" + 
                "Hull</span>, <em class=\"btitle\">Spanish and Russian Rivalry \n" + 
                "in the North Pacific Regions of the New World</em>, University of \n" + 
                "Alabama PhD thesis, UMI microfilm, \n" + 
                "<span class=\"spage\">112</span>–<span class=\"epage\">113</span></span>");
        Citation citation = Citation.fromDocument(URI.create("http://example.com/article"), reader).get(0);
        assertThat(citation.getGenre(), equalTo(Genre.thesis));
        assertThat(citation.getOpenurlField("au"), equalTo(Arrays.asList("Anthony H. Hull")));
        assertThat(citation.getOpenurlField("btitle"), equalTo(Arrays.asList(
                "Spanish and Russian Rivalry in the North Pacific Regions of the New World")));
        assertThat(citation.getOpenurlField("spage"), equalTo(Arrays.asList("112")));
        assertThat(citation.getOpenurlField("epage"), equalTo(Arrays.asList("113")));
    }
    
    @Test
    public void testProceedingfromDocument() throws Exception {
        XMLEventReader reader = x("<span xmlns=\"http://www.w3.org/1999/xhtml\" class=\"citation proceeding\">" +
                "<span class=\"au\">Valery O. \n" + 
                "Shubin</span>, ‘<span class=\"atitle\">Russian Settlements in the \n" + 
                "Kuril Islands in the 18th and 19th centuries</span>’, \n" + 
                "<em class=\"btitle\">Russia in North America: Proceedings of \n" + 
                "the 2nd International Conference on Russian America</em> \n" + 
                "(<span class=\"place\">Kingston and Fairbanks</span>: \n" + 
                "<span class=\"pub\">Limestone Press</span>, \n" + 
                "<span class=\"date\">1990</span>), \n" + 
                "<span class=\"spage\">425</span>–<span class=\"epage\">450</span>" +
                "<span class=\"cites\" title=\"books/shubin-1990\" /></span>");
        Citation citation = Citation.fromDocument(URI.create("http://example.com/article"), reader).get(0);
        assertThat(citation.getCites(), equalTo(
                Collections.singleton(URI.create("http://miskinhill.com.au/cited/books/shubin-1990"))));
        assertThat(citation.getGenre(), equalTo(Genre.proceeding));
        assertThat(citation.getOpenurlField("au"), equalTo(Arrays.asList("Valery O. Shubin")));
        assertThat(citation.getOpenurlField("atitle"), equalTo(Arrays.asList(
                "Russian Settlements in the Kuril Islands in the 18th and 19th centuries")));
        assertThat(citation.getOpenurlField("btitle"), equalTo(Arrays.asList(
                "Russia in North America: Proceedings of the 2nd International Conference on Russian America")));
        assertThat(citation.getOpenurlField("place"), equalTo(Arrays.asList("Kingston and Fairbanks")));
        assertThat(citation.getOpenurlField("pub"), equalTo(Arrays.asList("Limestone Press")));
        assertThat(citation.getOpenurlField("date"), equalTo(Arrays.asList("1990")));
        assertThat(citation.getOpenurlField("spage"), equalTo(Arrays.asList("425")));
        assertThat(citation.getOpenurlField("epage"), equalTo(Arrays.asList("450")));
    }
    
    @Test
    public void testArticlefromDocument() throws Exception {
        XMLEventReader reader = x("<span xmlns=\"http://www.w3.org/1999/xhtml\" class=\"citation article\">" +
                "<span class=\"au\" lang=\"ru\">Ал.&#160;П. \n" + 
                "Соколов</span>, «<span class=\"atitle\" lang=\"ru\">Приготовление \n" + 
                "кругосветной экспедиции 1787 года, под начальством Муловского</span>», \n" + 
                "<em class=\"jtitle\" lang=\"ru\">Записки Гидрографического Департамента \n" + 
                "Морского Министерства</em>, \n" + 
                "<span lang=\"ru\">часть&#160;<span class=\"volume\" title=\"6\">VI</span></span>, \n" + 
                "<span class=\"date\">1848</span>&#160;г., \n" + 
                "<span class=\"spage\">142</span>–<span class=\"epage\">191</span>" +
                "<span class=\"cites\" title=\"journals/zgdmm/6/prigotovlenie\" /></span>");
        Citation citation = Citation.fromDocument(URI.create("http://example.com/article"), reader).get(0);
        assertThat(citation.getCites(), equalTo(
                Collections.singleton(URI.create("http://miskinhill.com.au/cited/journals/zgdmm/6/prigotovlenie"))));
        assertThat(citation.getGenre(), equalTo(Genre.article));
        assertThat(citation.getOpenurlField("au"), equalTo(Arrays.asList("Ал. П. Соколов")));
        assertThat(citation.getOpenurlField("atitle"), equalTo(Arrays.asList(
                "Приготовление кругосветной экспедиции 1787 года, под начальством Муловского")));
        assertThat(citation.getOpenurlField("jtitle"), equalTo(Arrays.asList(
                "Записки Гидрографического Департамента Морского Министерства")));
        assertThat(citation.getOpenurlField("volume"), equalTo(Arrays.asList("6")));
        assertThat(citation.getOpenurlField("date"), equalTo(Arrays.asList("1848")));
        assertThat(citation.getOpenurlField("spage"), equalTo(Arrays.asList("142")));
        assertThat(citation.getOpenurlField("epage"), equalTo(Arrays.asList("191")));
    }
    
    @Test
    public void testBookCoinsValue() {
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        openurlFields.put("au", Arrays.asList("Charles Vinicombe Penrose", "Christopher Lloyd", "R. C. Anderson"));
        openurlFields.put("btitle", Arrays.asList("A Memoir of James Trevenen"));
        openurlFields.put("place", Arrays.asList("London"));
        openurlFields.put("pub", Arrays.asList("Navy Records Society"));
        openurlFields.put("date", Arrays.asList("1959"));
        openurlFields.put("spage", Arrays.asList("90"));
        openurlFields.put("epage", Arrays.asList("91"));
        Citation citation = new Citation(URI.create("http://example.com/article"),
                URI.create("http://example.com/article#citation-1"),
                Collections.<URI>emptySet(), openurlFields, Genre.book);
        assertEquals("ctx_ver=Z39.88-2004&rft.genre=book&rft_val_format=info%3Aofi%2Ffmt%3Akev%3Amtx%3Abook&" +
        		"rft.au=Charles+Vinicombe+Penrose&rft.au=Christopher+Lloyd&rft.au=R.+C.+Anderson&" +
        		"rft.btitle=A+Memoir+of+James+Trevenen&rft.place=London&rft.pub=Navy+Records+Society&" +
        		"rft.date=1959&rft.spage=90&rft.epage=91",
                citation.coinsValue());
    }
    
    @Test
    public void testBookitemCoinsValue() {
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        openurlFields.put("au", Arrays.asList("Lydia Black", "Robin Inglis"));
        openurlFields.put("atitle", Arrays.asList("“The Russians were Coming…”"));
        openurlFields.put("btitle", Arrays.asList("Spain and the North Pacific Coast"));
        openurlFields.put("place", Arrays.asList("Vancouver"));
        openurlFields.put("pub", Arrays.asList("Maritime Museum Society"));
        openurlFields.put("date", Arrays.asList("1992"));
        openurlFields.put("spage", Arrays.asList("31"));
        openurlFields.put("epage", Arrays.asList("29")); // sic
        Citation citation = new Citation(URI.create("http://example.com/article"),
                URI.create("http://example.com/article#citation-1"),
                Collections.<URI>emptySet(), openurlFields, Genre.bookitem);
        assertEquals("ctx_ver=Z39.88-2004&rft.genre=bookitem&rft_val_format=info%3Aofi%2Ffmt%3Akev%3Amtx%3Abook&" +
        		"rft.au=Lydia+Black&rft.au=Robin+Inglis&rft.atitle=%E2%80%9CThe+Russians+were+Coming%E2%80%A6%E2%80%9D&" +
        		"rft.btitle=Spain+and+the+North+Pacific+Coast&rft.place=Vancouver&rft.pub=Maritime+Museum+Society&" +
        		"rft.date=1992&rft.spage=31&rft.epage=29",
                citation.coinsValue());
    }
    
    @Test
    public void testThesisCoinsValue() {
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        openurlFields.put("au", Arrays.asList("Anthony H. Hull"));
        openurlFields.put("btitle", Arrays.asList("Spanish and Russian Rivalry in the North Pacific Regions of the New World"));
        openurlFields.put("spage", Arrays.asList("112"));
        openurlFields.put("epage", Arrays.asList("113"));
        Citation citation = new Citation(URI.create("http://example.com/article"),
                URI.create("http://example.com/article#citation-1"),
                Collections.<URI>emptySet(), openurlFields, Genre.thesis);
        assertEquals("ctx_ver=Z39.88-2004&rft.genre=document&rft_val_format=info%3Aofi%2Ffmt%3Akev%3Amtx%3Abook&" +
        		"rft.au=Anthony+H.+Hull&rft.btitle=Spanish+and+Russian+Rivalry+in+the+North+Pacific+Regions+" +
        		"of+the+New+World&rft.spage=112&rft.epage=113",
                citation.coinsValue());
    }
    
    @Test
    public void testProceedingCoinsValue() {
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        openurlFields.put("au", Arrays.asList("Valery O. Shubin"));
        openurlFields.put("atitle", Arrays.asList(
                "Russian Settlements in the Kuril Islands in the 18th and 19th centuries"));
        openurlFields.put("btitle", Arrays.asList("Russia in North America: Proceedings of the " +
        		"2nd International Conference on Russian America"));
        openurlFields.put("place", Arrays.asList("Kingston and Fairbanks"));
        openurlFields.put("pub", Arrays.asList("Limestone Press"));
        openurlFields.put("date", Arrays.asList("1990"));
        openurlFields.put("spage", Arrays.asList("425"));
        openurlFields.put("epage", Arrays.asList("450"));
        Citation citation = new Citation(URI.create("http://example.com/article"),
                URI.create("http://example.com/article#citation-1"),
                Collections.<URI>emptySet(), openurlFields, Genre.proceeding);
        assertEquals("ctx_ver=Z39.88-2004&rft.genre=proceeding&rft_val_format=info%3Aofi%2Ffmt%3Akev%3Amtx%3Abook&" +
        		"rft.au=Valery+O.+Shubin&rft.atitle=Russian+Settlements+in+the+Kuril+Islands+in+the+" +
        		"18th+and+19th+centuries&rft.btitle=Russia+in+North+America%3A+Proceedings+of+the+" +
        		"2nd+International+Conference+on+Russian+America&rft.place=Kingston+and+Fairbanks&" +
        		"rft.pub=Limestone+Press&rft.date=1990&rft.spage=425&rft.epage=450",
                citation.coinsValue());
    }
    
    @Test
    public void testArticleCoinsValue() {
        Map<String, List<String>> openurlFields = new LinkedHashMap<String, List<String>>();
        openurlFields.put("au", Arrays.asList("Ал. П. Соколов"));
        openurlFields.put("atitle", Arrays.asList(
                "Приготовление кругосветной экспедиции 1787 года, под начальством Муловского"));
        openurlFields.put("btitle", Arrays.asList(
                "Записки Гидрографического Департамента Морского Министерства"));
        openurlFields.put("volume", Arrays.asList("6"));
        openurlFields.put("date", Arrays.asList("1848"));
        openurlFields.put("spage", Arrays.asList("142"));
        openurlFields.put("epage", Arrays.asList("191"));
        Citation citation = new Citation(URI.create("http://example.com/article"),
                URI.create("http://example.com/article#citation-1"),
                Collections.<URI>emptySet(), openurlFields, Genre.article);
        assertEquals("ctx_ver=Z39.88-2004&rft.genre=article&rft_val_format=info%3Aofi%2Ffmt%3Akev%3Amtx%3Ajournal&" +
        		"rft.au=%D0%90%D0%BB.+%D0%9F.+%D0%A1%D0%BE%D0%BA%D0%BE%D0%BB%D0%BE%D0%B2&" +
        		"rft.atitle=%D0%9F%D1%80%D0%B8%D0%B3%D0%BE%D1%82%D0%BE%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5+" +
        		"%D0%BA%D1%80%D1%83%D0%B3%D0%BE%D1%81%D0%B2%D0%B5%D1%82%D0%BD%D0%BE%D0%B9+" +
        		"%D1%8D%D0%BA%D1%81%D0%BF%D0%B5%D0%B4%D0%B8%D1%86%D0%B8%D0%B8+1787+" +
        		"%D0%B3%D0%BE%D0%B4%D0%B0%2C+%D0%BF%D0%BE%D0%B4+%D0%BD%D0%B0%D1%87%D0%B0" +
        		"%D0%BB%D1%8C%D1%81%D1%82%D0%B2%D0%BE%D0%BC+" +
        		"%D0%9C%D1%83%D0%BB%D0%BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE&" +
        		"rft.btitle=%D0%97%D0%B0%D0%BF%D0%B8%D1%81%D0%BA%D0%B8+" +
        		"%D0%93%D0%B8%D0%B4%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D1%84%D0%B8%D1%87" +
        		"%D0%B5%D1%81%D0%BA%D0%BE%D0%B3%D0%BE+" +
        		"%D0%94%D0%B5%D0%BF%D0%B0%D1%80%D1%82%D0%B0%D0%BC%D0%B5%D0%BD%D1%82%D0%B0+" +
        		"%D0%9C%D0%BE%D1%80%D1%81%D0%BA%D0%BE%D0%B3%D0%BE+" +
        		"%D0%9C%D0%B8%D0%BD%D0%B8%D1%81%D1%82%D0%B5%D1%80%D1%81%D1%82%D0%B2%D0%B0&" +
        		"rft.volume=6&rft.date=1848&rft.spage=142&rft.epage=191",
                citation.coinsValue());
    }

}