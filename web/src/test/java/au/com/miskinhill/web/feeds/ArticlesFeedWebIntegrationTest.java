package au.com.miskinhill.web.feeds;

import static au.com.miskinhill.MiskinHillMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.joda.time.DateTime;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class ArticlesFeedWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void titleShouldBeThere() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(xpath("/atom:feed/atom:title[@type='text']").selectSingleNode(doc).getText(),
                equalTo("Miskin Hill journal articles"));
    }
    
    @Test
    public void feedIdShouldBeCorrect() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(xpath("/atom:feed/atom:id").selectSingleNode(doc).getText(),
                equalTo("http://miskinhill.com.au/feeds/articles"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void htmlLinksShouldNotIncludeExtension() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        List<Element> htmlLinks = xpath("/atom:feed/atom:entry/atom:link[@type='text/html' and @rel='alternate']").selectNodes(doc);
        assertFalse(htmlLinks.isEmpty());
        for (Element htmlLink: htmlLinks) {
            assertFalse(htmlLink.attributeValue("href").endsWith(".html"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void entriesShouldBeInOrderOfPublication() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        List<Element> publisheds = xpath("/atom:feed/atom:entry/atom:published").selectNodes(doc);
        List<DateTime> publishedTimes = new ArrayList<DateTime>();
        for (Element published: publisheds)
            publishedTimes.add(new DateTime(published.getTextTrim()));
        assertThat(publishedTimes, decreasingOrder(DateTime.class));
    }
    
    @Test
    public void journalSpecificFeedShouldHaveJournalNameInTitle() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .queryParam("journal", "http://miskinhill.com.au/journals/asees/")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(xpath("/atom:feed/atom:title[@type='text']").selectSingleNode(doc).getText(),
                equalTo("Australian Slavonic and East European Studies journal articles"));
    }
    
    @Test
    public void journalSpecificFeedShouldHaveUniqueId() throws DocumentException {
        String response = Client.create().resource(BASE).path("/feeds/articles")
                .queryParam("journal", "http://miskinhill.com.au/journals/asees/")
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
        Document doc = DocumentHelper.parseText(response);
        assertThat(xpath("/atom:feed/atom:id").selectSingleNode(doc).getText(),
                equalTo("http://miskinhill.com.au/feeds/articles?journal=http%3A%2F%2Fmiskinhill.com.au%2Fjournals%2Fasees%2F"));
    }
    
    @Test
    public void shouldGive404ForNonsenseJournal() throws DocumentException {
        try {
            Client.create().resource(BASE).path("/feeds/articles")
                    .queryParam("journal", "http://example.com/")
                    .accept(MediaType.APPLICATION_ATOM_XML_TYPE).get(String.class);
            fail("should throw");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), equalTo(HttpServletResponse.SC_NOT_FOUND));
        }
    }
    
    private XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("atom", "http://www.w3.org/2005/Atom"));
        return xpath;
    }

}
