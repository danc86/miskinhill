package au.com.miskinhill.rdf;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.com.miskinhill.rdf.vocabulary.MHS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/au/com/miskinhill/web/test-spring-context-with-fake-model.xml")
public class HTMLRepresentationTest {
    
    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    @Autowired private Model model;
    
    @Before
    public void setUp() throws Exception {
        representation = representationFactory.getRepresentationByFormat("html");
    }
    
    @Test
    public void testJournal() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Journal.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testIssue() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Issue.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testAuthor() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/authors/test-author"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Author.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void authorShouldLinkToWikipedia() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/authors/test-author"));
        Document doc = DocumentHelper.parseText(result);
        Element link = (Element) xpath("//html:div[@class='author-meta metabox']//html:a[text()='Wikipedia']").selectSingleNode(doc);
        assertThat(link.attributeValue("href"), equalTo("http://en.wikipedia.org/wiki/Test_Author"));
    }
    
    @Test
    public void testForum() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Forum.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testClass() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/Book"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Class.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testProperty() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/rdfschema/1.0/startPage"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Property.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testBook() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/cited/books/test"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Book.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void book_should_not_have_nested_hyperlinks() throws Exception {
        Resource book = model.createResource("http://miskinhill.com.au/cited/books/nested-hyperlinks");
        model.add(book, RDF.type, MHS.Book);
        String title = "<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                "this has a <a href=\"http://elsewhere.invalid\">nested anchor</a></span>";
        model.addLiteral(book, DCTerms.title, model.createLiteral(title, true));
        model.add(book, MHS.availableFrom, model.createResource("http://nowhere.invalid/the-book"));
        String result = representation.render(book);
        Document doc = DocumentHelper.parseText(result);
        assertThat(xpath("count(//html:a[@href]//html:a[@href])").numberValueOf(doc).intValue(), equalTo(0));
    }
    
    @Test
    public void testReview() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Review.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void reviewShouldHaveDCMetaTags() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review"));
        Document doc = DocumentHelper.parseText(result);
        Attribute profile = (Attribute) xpath("//html:head/@profile").selectSingleNode(doc);
        assertThat(Arrays.asList(profile.getValue().split("\\s")), hasItem("http://dublincore.org/documents/2008/08/04/dc-html/"));
        Element dcTitle = (Element) xpath("//html:head/html:meta[@name='DC.title']").selectSingleNode(doc);
        assertThat(dcTitle.getText() /* commonwrapper XSLT fixes this up */, equalTo("Review of Slovenia: evolving loyalties"));
        List<Element> dcCreators = xpath("//html:head/html:meta[@name='DC.creator']").selectNodes(doc);
        assertThat(dcCreators.size(), equalTo(1));
        assertThat(dcCreators.get(0).attributeValue("content"), equalTo("Test Author"));
        Element dcDate = (Element) xpath("//html:head/html:meta[@name='DC.date']").selectSingleNode(doc);
        assertThat(dcDate.attributeValue("content"), equalTo("2008-02-01"));
    }
    
    @Test
    public void reviewShouldHaveGoogleScholarMetaTags() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review"));
        Document doc = DocumentHelper.parseText(result);
        Element citationTitle = (Element) xpath("//html:head/html:meta[@name='citation_title']").selectSingleNode(doc);
        assertThat(citationTitle.getText() /* commonwrapper XSLT fixes this up */, equalTo("Review of Slovenia: evolving loyalties"));
        Element citationAuthors = (Element) xpath("//html:head/html:meta[@name='citation_authors']").selectSingleNode(doc);
        assertThat(citationAuthors.getText() /* commonwrapper XSLT fixes this up */, equalTo("Test Author"));
        Element citationPdfUrl = (Element) xpath("//html:head/html:meta[@name='citation_pdf_url']").selectSingleNode(doc);
        assertThat(citationPdfUrl.attributeValue("content"), equalTo("http://miskinhill.com.au/journals/test/1:1/reviews/review.pdf"));
        Element citationFirstPage = (Element) xpath("//html:head/html:meta[@name='citation_firstpage']").selectSingleNode(doc);
        assertThat(citationFirstPage.attributeValue("content"), equalTo("115"));
        Element citationLastPage = (Element) xpath("//html:head/html:meta[@name='citation_lastpage']").selectSingleNode(doc);
        assertThat(citationLastPage.attributeValue("content"), equalTo("116"));
        Element citationVolume = (Element) xpath("//html:head/html:meta[@name='citation_volume']").selectSingleNode(doc);
        assertThat(citationVolume.attributeValue("content"), equalTo("1"));
        Element citationIssue = (Element) xpath("//html:head/html:meta[@name='citation_issue']").selectSingleNode(doc);
        assertThat(citationIssue.attributeValue("content"), equalTo("1"));
        Element citationDate = (Element) xpath("//html:head/html:meta[@name='citation_date']").selectSingleNode(doc);
        assertThat(citationDate.attributeValue("content"), equalTo("2008-02-01"));
        Element citationIssn = (Element) xpath("//html:head/html:meta[@name='citation_issn']").selectSingleNode(doc);
        assertThat(citationIssn.attributeValue("content"), equalTo("12345678"));
        Element citationJournalTitle = (Element) xpath("//html:head/html:meta[@name='citation_journal_title']").selectSingleNode(doc);
        assertThat(citationJournalTitle.attributeValue("content"), equalTo("Test Journal of Good Stuff"));
        Element citationPublisher = (Element) xpath("//html:head/html:meta[@name='citation_publisher']").selectSingleNode(doc);
        assertThat(citationPublisher.attributeValue("content"), equalTo("Awesome Publishing House"));
    }
    
    @Test
    public void reviewShouldHavePublicationDate() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review"));
        Document doc = DocumentHelper.parseText(result);
        assertThat(xpath("//html:span[@class='date']").selectSingleNode(doc).getText(), equalTo("1 February 2008"));
    }
    
    @Test
    public void testArticle() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Article.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void articleShouldHaveDCMetaTags() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        Document doc = DocumentHelper.parseText(result);
        Attribute profile = (Attribute) xpath("//html:head/@profile").selectSingleNode(doc);
        assertThat(Arrays.asList(profile.getValue().split("\\s")), hasItem("http://dublincore.org/documents/2008/08/04/dc-html/"));
        Element dcTitle = (Element) xpath("//html:head/html:meta[@name='DC.title']").selectSingleNode(doc);
        assertThat(dcTitle.attributeValue("content"), equalTo("Moscow 1937: the interpreter’s story"));
        List<Element> dcCreators = xpath("//html:head/html:meta[@name='DC.creator']").selectNodes(doc);
        assertThat(dcCreators.size(), equalTo(1));
        assertThat(dcCreators.get(0).attributeValue("content"), equalTo("Test Author"));
        Element dcDate = (Element) xpath("//html:head/html:meta[@name='DC.date']").selectSingleNode(doc);
        assertThat(dcDate.attributeValue("content"), equalTo("2008-02-01"));
    }
    
    /**
     * http://blog.reallywow.com/archives/123
     */
    @Test
    public void articleShouldHaveGoogleScholarMetaTags() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        Document doc = DocumentHelper.parseText(result);
        Element citationTitle = (Element) xpath("//html:head/html:meta[@name='citation_title']").selectSingleNode(doc);
        assertThat(citationTitle.attributeValue("content"), equalTo("Moscow 1937: the interpreter’s story"));
        Element citationAuthors = (Element) xpath("//html:head/html:meta[@name='citation_authors']").selectSingleNode(doc);
        assertThat(citationAuthors.getText() /* commonwrapper XSLT fixes this up */, equalTo("Test Author"));
        Element citationPdfUrl = (Element) xpath("//html:head/html:meta[@name='citation_pdf_url']").selectSingleNode(doc);
        assertThat(citationPdfUrl.attributeValue("content"), equalTo("http://miskinhill.com.au/journals/test/1:1/article.pdf"));
        Element citationFirstPage = (Element) xpath("//html:head/html:meta[@name='citation_firstpage']").selectSingleNode(doc);
        assertThat(citationFirstPage.attributeValue("content"), equalTo("5"));
        Element citationLastPage = (Element) xpath("//html:head/html:meta[@name='citation_lastpage']").selectSingleNode(doc);
        assertThat(citationLastPage.attributeValue("content"), equalTo("35"));
        Element citationVolume = (Element) xpath("//html:head/html:meta[@name='citation_volume']").selectSingleNode(doc);
        assertThat(citationVolume.attributeValue("content"), equalTo("1"));
        Element citationIssue = (Element) xpath("//html:head/html:meta[@name='citation_issue']").selectSingleNode(doc);
        assertThat(citationIssue.attributeValue("content"), equalTo("1"));
        Element citationDate = (Element) xpath("//html:head/html:meta[@name='citation_date']").selectSingleNode(doc);
        assertThat(citationDate.attributeValue("content"), equalTo("2008-02-01"));
        Element citationIssn = (Element) xpath("//html:head/html:meta[@name='citation_issn']").selectSingleNode(doc);
        assertThat(citationIssn.attributeValue("content"), equalTo("12345678"));
        Element citationJournalTitle = (Element) xpath("//html:head/html:meta[@name='citation_journal_title']").selectSingleNode(doc);
        assertThat(citationJournalTitle.attributeValue("content"), equalTo("Test Journal of Good Stuff"));
        Element citationPublisher = (Element) xpath("//html:head/html:meta[@name='citation_publisher']").selectSingleNode(doc);
        assertThat(citationPublisher.attributeValue("content"), equalTo("Awesome Publishing House"));
    }
    
    
    @Test
    public void articleShouldHavePublicationDate() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        Document doc = DocumentHelper.parseText(result);
        assertThat(xpath("//html:span[@class='date']").selectSingleNode(doc).getText(), equalTo("1 February 2008"));
    }
    
    @Test
    public void articleSubjectListingShouldHandleTripleHyphen() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/article"));
        Document doc = DocumentHelper.parseText(result);
        assertThat(xpath("//html:div[@class='subjects']/html:ul/html:li[1]").selectSingleNode(doc).getText(),
                equalTo("Makine, Andreï, 1957- – Criticism and interpretation"));
    }
    
    @Test
    public void testObituary() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/Obituary.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void obituaryShouldHaveDCMetaTags() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe"));
        Document doc = DocumentHelper.parseText(result);
        Attribute profile = (Attribute) xpath("//html:head/@profile").selectSingleNode(doc);
        assertThat(Arrays.asList(profile.getValue().split("\\s")), hasItem("http://dublincore.org/documents/2008/08/04/dc-html/"));
        Element dcTitle = (Element) xpath("//html:head/html:meta[@name='DC.title']").selectSingleNode(doc);
        assertThat(dcTitle.attributeValue("content"), equalTo("In memoriam John Doe"));
        List<Element> dcCreators = xpath("//html:head/html:meta[@name='DC.creator']").selectNodes(doc);
        assertThat(dcCreators.size(), equalTo(1));
        assertThat(dcCreators.get(0).attributeValue("content"), equalTo("Test Author"));
        Element dcDate = (Element) xpath("//html:head/html:meta[@name='DC.date']").selectSingleNode(doc);
        assertThat(dcDate.attributeValue("content"), equalTo("2008-02-01"));
    }
    
    @Test
    public void obituaryShouldHavePublicationDate() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/1:1/in-memoriam-john-doe"));
        Document doc = DocumentHelper.parseText(result);
        assertThat(xpath("//html:span[@class='date']").selectSingleNode(doc).getText(), equalTo("1 February 2008"));
    }
    
    @Test
    public void testCitedArticle() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/cited/journals/asdf/1:1/article"));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("template/html/CitedArticle.out.xml"), "UTF-8");
        assertEquals(expected.trim(), result.trim());
    }
    
    private XPath xpath(String expression) {
        XPath xpath = DocumentHelper.createXPath(expression);
        xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
        return xpath;
    }

}
