package au.com.miskinhill.web.oaipmh;

import static au.com.miskinhill.MiskinHillMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.hamcrest.Description;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.web.ProperURLCodec;

public class OaipmhWebIntegrationTest extends AbstractWebIntegrationTest {
    
    private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed();
    
    @Test
    public void shouldServeTextXml() {
        ResponseEntity<Document> response = restTemplate.getForEntity(BASE.resolve("/oaipmh?verb=Identify"), Document.class);
        assertThat(response.getHeaders().getContentType(), equalTo(MediaType.TEXT_XML));
    }
    
    @Test
    public void shouldAcceptPOST() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("verb", "Identify");
        Document doc = restTemplate.postForObject(BASE.resolve("/oaipmh"), params, Document.class);
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("Identify"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
    }
    
    @Test
    public void testNoVerb() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), nullValue());
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badVerb"));
        assertThat(error.getText(), equalTo("verb parameter missing"));
    }
    
    @Test
    public void testNonsenseVerb() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=asdf"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), nullValue());
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badVerb"));
        assertThat(error.getText(), equalTo("Unrecognised verb asdf"));
    }
    
    @Test
    public void testIdentify() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=Identify"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("Identify"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:repositoryName").selectSingleNode(doc).getText(), containsString("Miskin Hill"));
        assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:baseURL").selectSingleNode(doc).getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:protocolVersion").selectSingleNode(doc).getText(), equalTo("2.0"));
        assertTrue(DATE_TIME_FORMAT.parseDateTime(
                xpath("/oai:OAI-PMH/oai:Identify/oai:earliestDatestamp").selectSingleNode(doc).getText())
                .isBeforeNow());
        assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:deletedRecord").selectSingleNode(doc).getText(), equalTo("no"));
        assertThat(xpath("/oai:OAI-PMH/oai:Identify/oai:granularity").selectSingleNode(doc).getText(), equalTo("YYYY-MM-DDThh:mm:ssZ"));
    }
    
    @Test
    public void testListMetadataFormatsForAllItems() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListMetadataFormats"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        @SuppressWarnings("unchecked")
        List<Element> formats = (List<Element>) xpath("/oai:OAI-PMH/oai:ListMetadataFormats/oai:metadataFormat").selectNodes(doc);
        assertThat(formats, hasItems(
                new MetadataFormatMatcher("oai_dc",
                    "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                    "http://www.openarchives.org/OAI/2.0/oai_dc/")));
    }
    
    @Test
    public void testListMetadataFormatsForNonExistentId() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListMetadataFormats&identifier=asdf"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
        assertThat(request.attributeValue("identifier"), equalTo("asdf"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("idDoesNotExist"));
        assertThat(error.getText(), equalTo("Identifier asdf is not known to this repository"));
    }
    
    @Test
    public void testListMetadataFormatsForId() {
        String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
        Document doc = restTemplate.getForObject(
                BASE.resolve("/oaipmh?verb=ListMetadataFormats&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListMetadataFormats"));
        assertThat(request.attributeValue("identifier"), equalTo(articleUri));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        @SuppressWarnings("unchecked")
        List<Element> formats = (List<Element>) xpath("/oai:OAI-PMH/oai:ListMetadataFormats/oai:metadataFormat").selectNodes(doc);
        assertThat(formats, hasItems(
                new MetadataFormatMatcher("oai_dc",
                        "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                "http://www.openarchives.org/OAI/2.0/oai_dc/")));
    }
    
    @Test
    public void testListIdentifiersWithoutMetadataPrefix() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badArgument"));
        assertThat(error.getText(), equalTo("metadataPrefix parameter missing"));
    }
    
    @Test
    public void testListIdentifiers() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=oai_dc"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
        assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        @SuppressWarnings("unchecked")
        List<Element> headers = (List<Element>) xpath("/oai:OAI-PMH/oai:ListIdentifiers/oai:header").selectNodes(doc);
        assertThat(headers.size(), greaterThan(131));
        assertThat(headers, hasItem(new HeaderMatcher("http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik")));
    }
    
    @Test
    public void testListIdentifiersForUnsupportedMetadataPrefix() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=ListIdentifiers&metadataPrefix=asdf"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("ListIdentifiers"));
        assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("cannotDisseminateFormat"));
        assertThat(error.getText(), equalTo("Metadata prefix asdf is not supported"));
    }
    
    @Test
    public void testGetRecordWithoutIdentifierOrMetadataPrefix() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), nullValue());
        assertThat(request.attributeValue("metadataPrefix"), nullValue());
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badArgument"));
        assertThat(error.getText(), equalTo("identifier parameter missing"));
    }
    
    @Test
    public void testGetRecordWithoutIdentifier() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), nullValue());
        assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badArgument"));
        assertThat(error.getText(), equalTo("identifier parameter missing"));
    }
    
    @Test
    public void testGetRecordWithoutMetadataPrefix() {
        String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
        Document doc = restTemplate.getForObject(
                BASE.resolve("/oaipmh?verb=GetRecord&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), equalTo(articleUri));
        assertThat(request.attributeValue("metadataPrefix"), nullValue());
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("badArgument"));
        assertThat(error.getText(), equalTo("metadataPrefix parameter missing"));
    }
    
    @Test
    public void testGetRecordForNonsenseMetadataPrefix() {
        String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
        Document doc = restTemplate.getForObject(
                BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=asdf&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), equalTo(articleUri));
        assertThat(request.attributeValue("metadataPrefix"), equalTo("asdf"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("cannotDisseminateFormat"));
        assertThat(error.getText(), equalTo("Metadata prefix asdf is not supported"));
    }
    
    @Test
    public void testGetRecordForNonexistentId() {
        Document doc = restTemplate.getForObject(BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc&identifier=asdf"), Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), equalTo("asdf"));
        assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        Element error = (Element) xpath("/oai:OAI-PMH/oai:error").selectSingleNode(doc);
        assertThat(error.attributeValue("code"), equalTo("idDoesNotExist"));
        assertThat(error.getText(), equalTo("Identifier asdf is not known to this repository"));
    }
    
    @Test
    public void testGetRecord() {
        String articleUri = "http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik";
        Document doc = restTemplate.getForObject(
                BASE.resolve("/oaipmh?verb=GetRecord&metadataPrefix=oai_dc&identifier=" + ProperURLCodec.encodeUrl(articleUri)),
                Document.class);
        assertResponseDate(doc);
        
        Element request = (Element) xpath("/oai:OAI-PMH/oai:request").selectSingleNode(doc);
        assertThat(request.attributeValue("verb"), equalTo("GetRecord"));
        assertThat(request.attributeValue("identifier"), equalTo(articleUri));
        assertThat(request.attributeValue("metadataPrefix"), equalTo("oai_dc"));
        assertThat(request.getText(), equalTo("http://miskinhill.com.au/oaipmh"));
        
        @SuppressWarnings("unchecked")
        List<Element> records = (List<Element>) xpath("/oai:OAI-PMH/oai:GetRecord/oai:record").selectNodes(doc);
        assertThat(records.size(), equalTo(1));
        Element record = records.get(0);
        assertThat((Element) xpath("./oai:header").selectSingleNode(record), new HeaderMatcher(articleUri));
        assertThat(xpath("./oai:metadata/oai_dc:dc/*").selectNodes(record).size(), greaterThan(0));
    }
    
    private static XPath xpath(String expression) { // ugh
        XPath xpath = DocumentHelper.createXPath(expression);
        HashMap<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("oai", "http://www.openarchives.org/OAI/2.0/");
        namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        xpath.setNamespaceURIs(namespaces);
        return xpath;
    }

    private void assertResponseDate(Document doc) {
        DateTime responseDate = DATE_TIME_FORMAT.parseDateTime(xpath("/oai:OAI-PMH/oai:responseDate").selectSingleNode(doc).getText());
        assertThat(responseDate, within(Duration.standardSeconds(5)));
        assertThat(responseDate.getZone(), equalTo(DateTimeZone.UTC));
    }
    
    private static final class MetadataFormatMatcher extends TypeSafeMatcher<Element> {
        
        private final String prefix;
        private final String schema;
        private final String namespace;
        
        public MetadataFormatMatcher(String prefix, String schema, String namespace) {
            this.prefix = prefix;
            this.schema = schema;
            this.namespace = namespace;
        }

        @Override
        public boolean matchesSafely(Element element) {
            return (xpath("./oai:metadataPrefix").selectSingleNode(element).getText().equals(prefix) &&
                    xpath("./oai:schema").selectSingleNode(element).getText().equals(schema) &&
                    xpath("./oai:metadataNamespace").selectSingleNode(element).getText().equals(namespace));
        }
        
        @Override
        public void describeTo(Description desc) {
            desc.appendText("<metadataFormat> element with <metadataPrefix>")
                .appendText(prefix)
                .appendText("</metadataPrefix> and <schema>")
                .appendText(schema)
                .appendText("</schema> and <metadataNamespace>")
                .appendText(namespace)
                .appendText("</metadataNamespace>");
        }
        
    }
    
    private static final class HeaderMatcher extends TypeSafeMatcher<Element> {
        
        private final String identifier;
        
        public HeaderMatcher(String identifier) {
            this.identifier = identifier;
        }
        
        @Override
        public boolean matchesSafely(Element element) {
            return (xpath("./oai:identifier").selectSingleNode(element).getText().equals(identifier) &&
                    DATE_TIME_FORMAT.parseDateTime(xpath("./oai:datestamp").selectSingleNode(element).getText())
                        .getZone().equals(DateTimeZone.UTC));
        }
        
        @Override
        public void describeTo(Description desc) {
            desc.appendText("<header> element with <identifier>")
                .appendText(identifier)
                .appendText("</identifier> with UTC datestamp");
        }
        
    }

}
