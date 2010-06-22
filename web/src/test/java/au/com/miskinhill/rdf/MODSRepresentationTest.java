package au.com.miskinhill.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context-with-fake-model.xml")
public class MODSRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    @Autowired private Model model;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("mods");
    }
    
    @Test
    public void markupShouldBeStrippedFromArticleTitle() throws Exception {
        Element title = (Element) xpath("/mods:modsCollection/mods:mods/mods:titleInfo/mods:title").selectSingleNode(renderArticle());
        assertThat(title.elements().size(), equalTo(0));
    }
    
    @Test
    public void articleShouldContainVolumeNumber() throws Exception {
        String volumeNumber = xpath("/mods:modsCollection/mods:mods/mods:relatedItem[@type='host']/mods:part/mods:detail[@type='volume']/mods:number")
                .selectSingleNode(renderArticle()).getText();
        assertThat(volumeNumber, equalTo("1"));
    }
    
    @Test
    public void articleShouldContainIssueNumber() throws Exception {
        String volumeNumber = xpath("/mods:modsCollection/mods:mods/mods:relatedItem[@type='host']/mods:part/mods:detail[@type='issue']/mods:number")
                .selectSingleNode(renderArticle()).getText();
        assertThat(volumeNumber, equalTo("1"));
    }
    
    @Test
    public void articleShouldContainLocationUrls() throws Exception {
        List<Element> urls = xpath("/mods:modsCollection/mods:mods/mods:location/mods:url").selectNodes(renderArticle());
        assertThat(urls.size(), equalTo(2));
        assertThat(urls.get(0).attributeValue("displayLabel"), equalTo("HTML version"));
        assertThat(urls.get(0).attributeValue("access"), equalTo("object in context"));
        assertThat(urls.get(0).getText(), equalTo("http://miskinhill.com.au/journals/test/1:1/article"));
        assertThat(urls.get(1).attributeValue("displayLabel"), equalTo("Original print version"));
        assertThat(urls.get(1).attributeValue("access"), equalTo("raw object"));
        assertThat(urls.get(1).getText(), equalTo("http://miskinhill.com.au/journals/test/1:1/article.pdf"));
    }
    
    @Test
    public void articleShouldSpecifyLanguageFromJournal() throws Exception {
        List<Element> langs = xpath("/mods:modsCollection/mods:mods/mods:language/mods:languageTerm[@type='code' and @authority='rfc3066']").selectNodes(renderArticle());
        assertThat(langs.size(), equalTo(2));
        assertThat(langs.get(0).getText(), equalTo("en"));
        assertThat(langs.get(1).getText(), equalTo("ru"));
    }
    
    @Test
    public void articleShouldSpecifyGenre() throws Exception {
        String genre = xpath("/mods:modsCollection/mods:mods/mods:genre[@authority='marcgt']").selectSingleNode(renderArticle()).getText();
        assertThat(genre, equalTo("periodical"));
    }
    
    @Test
    public void articleShouldContainJournalMetadata() throws Exception {
        String journalTitle = xpath("/mods:modsCollection/mods:mods/mods:relatedItem[@type='host']/mods:titleInfo/mods:title").selectSingleNode(renderArticle()).getText();
        assertThat(journalTitle, equalTo("Test Journal of Good Stuff"));
    }
    
    @Test
    public void journalShouldContainTitle() throws Exception {
        String journalTitle = xpath("/mods:modsCollection/mods:mods/mods:titleInfo/mods:title").selectSingleNode(renderJournal()).getText();
        assertThat(journalTitle, equalTo("Test Journal of Good Stuff"));
    }
    
    @Test
    public void journalShouldContainLanguages() throws Exception {
        List<Element> langs = xpath("/mods:modsCollection/mods:mods/mods:language/mods:languageTerm[@type='code' and @authority='rfc3066']").selectNodes(renderJournal());
        assertThat(langs.size(), equalTo(2));
        assertThat(langs.get(0).getText(), equalTo("en"));
        assertThat(langs.get(1).getText(), equalTo("ru"));
    }
    
    private Document renderArticle() throws DocumentException {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        return DocumentHelper.parseText(result);
    }
    
    private Document renderJournal() throws DocumentException {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        return DocumentHelper.parseText(result);
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("mods", "http://www.loc.gov/mods/v3"));
        return xpath;
    }

}
