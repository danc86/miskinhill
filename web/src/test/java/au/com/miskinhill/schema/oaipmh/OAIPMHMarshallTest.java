package au.com.miskinhill.schema.oaipmh;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.w3c.dom.Element;

import au.com.miskinhill.schema.oaiidentifier.OaiIdentifier;

public class OAIPMHMarshallTest {

	private static final JAXBContext jc;
	static {
		try {
			jc = JAXBContext.newInstance("au.com.miskinhill.schema.oaipmh:au.com.miskinhill.schema.oaiidentifier");
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void assertMarshalled(String expected, OAIPMH<?> oaipmh) throws JAXBException {
        StringWriter w = new StringWriter();
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new OAINamespacePrefixMapper());
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(oaipmh, w);
        assertEquals(expected, w.toString());
	}

	@Test
	public void shouldMarshallGetRecordResponse() throws Exception {
        Element oaidc = domElementFromString("<oai_dc:dc " +
                "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " + 
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                "<dc:title>Using Structural Metadata to Localize Experience of Digital Content</dc:title>" +
                "<dc:creator>Dushay, Naomi</dc:creator>" +
                "<dc:subject>Digital Libraries</dc:subject>" +
                "<dc:description>With the increasing technical sophistication of " + 
                "both information consumers and providers, there is " + 
                "increasing demand for more meaningful experiences of digital " + 
                "information. We present a framework that separates digital " + 
                "object experience, or rendering, from digital object storage " + 
                "and manipulation, so the rendering can be tailored to " + 
                "particular communities of users.</dc:description>" +
                "<dc:description>Comment: 23 pages including 2 appendices, 8 figures</dc:description>" +
                "<dc:date>2001-12-14T01:02:03Z</dc:date>" +
                "</oai_dc:dc>");
        Record record = new Record(
                new RecordHeader("oai:arXiv.org:cs/0112017",
                        new DateTime(2001, 12, 14, 1, 2, 3, 0, DateTimeZone.UTC),
                        Arrays.asList("cs", "math")),
                new Metadata(oaidc));
		OAIPMH<GetRecordResponse> oaipmh = new OAIPMH<GetRecordResponse>(
		        new DateTime(2002, 2, 8, 8, 55, 46, 0, DateTimeZone.UTC),
		        new Request.Builder(URI.create("http://arXiv.org/oai2")).forVerb(Verb.GET_RECORD)
		            .forIdentifier("oai:arXiv.org:cs/0112017").forMetadataPrefix("oai_dc").build(),
		        new GetRecordResponse(record));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("GetRecord-example.xml"), "UTF-8");
        assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallIdentifyResponse() throws Exception {
	    Description identifierDescription = new Description(new OaiIdentifier(
	            "oai", "lcoa1.loc.gov", ":", "oai:lcoa1.loc.gov:loc.music/musdi.002"));
        IdentifyResponse identify = new IdentifyResponse(
	            "Library of Congress Open Archive Initiative Repository 1",
	            URI.create("http://memory.loc.gov/cgi-bin/oai"),
	            new DateTime(1990, 2, 1, 12, 0, 0, 0, DateTimeZone.UTC),
	            Arrays.asList("somebody@loc.gov", "anybody@loc.gov"),
	            DeletedRecordSupport.TRANSIENT,
	            Granularity.DATE_TIME,
	            Arrays.asList("deflate"),
	            Arrays.asList(identifierDescription));
	    OAIPMH<IdentifyResponse> oaipmh = new OAIPMH<IdentifyResponse>(
	            new DateTime(2002, 2, 8, 12, 0, 1, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://memory.loc.gov/cgi-bin/oai")).forVerb(Verb.IDENTIFY).build(),
	            identify);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("Identify-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallListIdentifiersResponse() throws Exception {
	    ListIdentifiersResponse listIdentifiers = new ListIdentifiersResponse(Arrays.asList(
	            new RecordHeader("oai:arXiv.org:hep-th/9801001",
                    new DateTime(1999, 2, 23, 0, 0, 0, 0, DateTimeZone.UTC),
                    Arrays.asList("physic:hep")),
	            new RecordHeader("oai:arXiv.org:hep-th/9801002",
                    new DateTime(1999, 3, 20, 0, 0, 0, 0, DateTimeZone.UTC),
                    Arrays.asList("physic:hep", "physic:exp")),
	            new RecordHeader("oai:arXiv.org:hep-th/9801005",
                    new DateTime(2000, 1, 18, 0, 0, 0, 0, DateTimeZone.UTC),
                    Arrays.asList("physic:hep")),
	            new RecordHeader("oai:arXiv.org:hep-th/9801010",
                    new DateTime(1999, 2, 23, 0, 0, 0, 0, DateTimeZone.UTC),
                    Arrays.asList("physic:hep", "math"), Status.DELETED)),
	            new ResumptionToken("xxx45abttyz", new DateTime(2002, 6, 1, 23, 20, 0, 0, DateTimeZone.UTC), 6, 0));
	    OAIPMH<ListIdentifiersResponse> oaipmh = new OAIPMH<ListIdentifiersResponse>(
	            new DateTime(2002, 6, 1, 19, 20, 30, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://an.oa.org/OAI-script")).forVerb(Verb.LIST_IDENTIFIERS)
                    .from(new DateTime(1998, 1, 15, 0, 0, 0, 0, DateTimeZone.UTC))
                    .forMetadataPrefix("oldarXiv").forSet("physics:hep").build(),
	            listIdentifiers);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("ListIdentifiers-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallListMetadataFormatsResponse() throws Exception {
	    ListMetadataFormatsResponse listMetadataFormats = new ListMetadataFormatsResponse(Arrays.asList(
	            new MetadataFormat("oai_dc",
	                    URI.create("http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
	                    URI.create("http://www.openarchives.org/OAI/2.0/oai_dc/")),
	            new MetadataFormat("olac",
	                    URI.create("http://www.language-archives.org/OLAC/olac-0.2.xsd"),
	                    URI.create("http://www.language-archives.org/OLAC/0.2/")),
        	    new MetadataFormat("perseus",
        	            URI.create("http://www.perseus.tufts.edu/persmeta.xsd"),
        	            URI.create("http://www.perseus.tufts.edu/persmeta.dtd"))));
	    OAIPMH<ListMetadataFormatsResponse> oaipmh = new OAIPMH<ListMetadataFormatsResponse>(
	            new DateTime(2002, 2, 8, 14, 27, 19, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://www.perseus.tufts.edu/cgi-bin/pdataprov"))
	                .forVerb(Verb.LIST_METADATA_FORMATS).forIdentifier("oai:perseus.tufts.edu:Perseus:text:1999.02.0119").build(),
	            listMetadataFormats);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("ListMetadataFormats-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallListRecordsResponse() throws Exception {
	    ListRecordsResponse listRecords = new ListRecordsResponse(Arrays.asList(
	            new Record(
                    new RecordHeader("oai:perseus:Perseus:text:1999.02.0084",
                        new DateTime(2002, 5, 1, 14, 16, 12, 0, DateTimeZone.UTC)),
                    new Metadata(domElementFromString("<oai_dc:dc " +
                            "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " + 
                            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                            "<dc:title>Opera Minora</dc:title>" +
                            "<dc:creator>Cornelius Tacitus</dc:creator>" +
                            "<dc:type>text</dc:type>" +
                            "<dc:source>Opera Minora. Cornelius Tacitus. Henry Furneaux. Clarendon Press. Oxford. 1900.</dc:source>" +
                            "<dc:language>latin</dc:language>" +
                            "<dc:identifier>http://www.perseus.tufts.edu/cgi-bin/ptext?doc=Perseus:text:1999.02.0084</dc:identifier>" +
                            "</oai_dc:dc>"))),
                new Record(
                    new RecordHeader("oai:perseus:Perseus:text:1999.02.0083",
                        new DateTime(2002, 5, 1, 14, 20, 55, 0, DateTimeZone.UTC)),
                    new Metadata(domElementFromString("<oai_dc:dc " +
                            "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " + 
                            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                            "<dc:title>Germany and its Tribes</dc:title>" +
                            "<dc:creator>Tacitus</dc:creator>" +
                            "<dc:type>text</dc:type>" +
                            "<dc:source>Complete Works of Tacitus. Tacitus. Alfred John Church. " +
                            "William Jackson Brodribb. Lisa Cerrato. edited for Perseus. " +
                            "New York: Random House, Inc. Random House, Inc. reprinted 1942.</dc:source>" +
                            "<dc:language>english</dc:language>" +
                            "<dc:identifier>http://www.perseus.tufts.edu/cgi-bin/ptext?doc=Perseus:text:1999.02.0083</dc:identifier>" +
                            "</oai_dc:dc>")))));
	    OAIPMH<ListRecordsResponse> oaipmh = new OAIPMH<ListRecordsResponse>(
	            new DateTime(2002, 6, 1, 19, 20, 30, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://www.perseus.tufts.edu/cgi-bin/pdataprov")).forVerb(Verb.LIST_RECORDS)
                    .from(new DateTime(2002, 5, 1, 14, 15, 0, 0, DateTimeZone.UTC))
                    .until(new DateTime(2002, 5, 1, 14, 20, 0, 0, DateTimeZone.UTC))
                    .forMetadataPrefix("oai_dc").build(),
	            listRecords);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("ListRecords-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallListSetsResponse() throws Exception {
	    ListSetsResponse listSets = new ListSetsResponse(Arrays.asList(
	            new Set("music", "Music collection"),
	            new Set("music:(muzak)", "Muzak collection"),
	            new Set("music:(elec)", "Electronic Music Collection", Arrays.asList(
                    new Description(domElementFromString("<oai_dc:dc " +
                            "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " + 
                            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                            "<dc:description>This set contains metadata describing electronic music recordings made during the 1950ies</dc:description>" +
                            "</oai_dc:dc>")))),
                new Set("video", "Video Collection")));
	    OAIPMH<ListSetsResponse> oaipmh = new OAIPMH<ListSetsResponse>(
	            new DateTime(2002, 8, 11, 7, 21, 33, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://an.oa.org/OAI-script")).forVerb(Verb.LIST_SETS).build(),
                listSets);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("ListSets-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallError() throws Exception {
	    OAIPMH<ListSetsResponse> oaipmh = new OAIPMH<ListSetsResponse>(
	            new DateTime(2002, 5, 1, 9, 18, 29, 0, DateTimeZone.UTC),
	            new Request.Builder(URI.create("http://arXiv.org/oai2")).forVerb(Verb.LIST_SETS).build(),
	            Arrays.asList(new Error(ErrorCode.NO_SET_HIERARCHY, "This repository does not support sets")));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("error-example.xml"), "UTF-8");
        assertMarshalled(expected, oaipmh);
	}
	
	private Element domElementFromString(String xml) throws DocumentException {
	    return new DOMWriter().write(DocumentHelper.parseText(xml)).getDocumentElement();
	}

}
