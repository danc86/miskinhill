package au.com.miskinhill.web.feeds;

import static au.com.miskinhill.MiskinHillMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.web.ProperURLCodec;

public class ArticlesFeedWebIntegrationTest extends AbstractWebIntegrationTest {
    
    private static final String ASEES_JOURNAL_PARAM = "journal=" + ProperURLCodec.encodeUrl("http://miskinhill.com.au/journals/asees/");
    
    @Test
    public void titleShouldBeThere() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles"), Document.class);
        assertThat(xpath("/atom:feed/atom:title[@type='text']").selectSingleNode(doc).getText(),
                equalTo("Miskin Hill journal articles"));
    }
    
    @Test
    public void feedIdShouldBeCorrect() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles"), Document.class);
        assertThat(xpath("/atom:feed/atom:id").selectSingleNode(doc).getText(),
                equalTo("http://miskinhill.com.au/feeds/articles"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void htmlLinksShouldNotIncludeExtension() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles"), Document.class);
        List<Element> htmlLinks = xpath("/atom:feed/atom:entry/atom:link[@type='text/html' and @rel='alternate']").selectNodes(doc);
        assertFalse(htmlLinks.isEmpty());
        for (Element htmlLink: htmlLinks) {
            assertFalse(htmlLink.attributeValue("href").endsWith(".html"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void entriesShouldBeInOrderOfPublication() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles"), Document.class);
        List<Element> publisheds = xpath("/atom:feed/atom:entry/atom:published").selectNodes(doc);
        List<DateTime> publishedTimes = new ArrayList<DateTime>();
        for (Element published: publisheds)
            publishedTimes.add(new DateTime(published.getTextTrim()));
        assertThat(publishedTimes, decreasingOrder(DateTime.class));
    }
    
    @Test
    public void journalSpecificFeedShouldHaveJournalNameInTitle() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles?" + ASEES_JOURNAL_PARAM), Document.class);
        assertThat(xpath("/atom:feed/atom:title[@type='text']").selectSingleNode(doc).getText(),
                equalTo("Australian Slavonic and East European Studies journal articles"));
    }
    
    @Test
    public void journalSpecificFeedShouldHaveUniqueId() throws DocumentException {
        Document doc = restTemplate.getForObject(BASE.resolve("/feeds/articles?" + ASEES_JOURNAL_PARAM), Document.class);
        assertThat(xpath("/atom:feed/atom:id").selectSingleNode(doc).getText(),
                equalTo("http://miskinhill.com.au/feeds/articles?journal=http%3A%2F%2Fmiskinhill.com.au%2Fjournals%2Fasees%2F"));
    }
    
    @Test
    public void shouldGive404ForNonsenseJournal() throws DocumentException {
        assertHttpError(BASE.resolve("/feeds/articles?journal=" + ProperURLCodec.encodeUrl("http://example.com/")),
                HttpStatus.NOT_FOUND);
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("atom", "http://www.w3.org/2005/Atom"));
        return xpath;
    }

}
