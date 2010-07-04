package au.com.miskinhill.schema.oaipmh;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import au.com.miskinhill.schema.oaidc.AbstractElement;
import au.com.miskinhill.schema.oaidc.Creator;
import au.com.miskinhill.schema.oaidc.Date;
import au.com.miskinhill.schema.oaidc.Description;
import au.com.miskinhill.schema.oaidc.Identifier;
import au.com.miskinhill.schema.oaidc.Language;
import au.com.miskinhill.schema.oaidc.OaiDc;
import au.com.miskinhill.schema.oaidc.Source;
import au.com.miskinhill.schema.oaidc.Subject;
import au.com.miskinhill.schema.oaidc.Title;
import au.com.miskinhill.schema.oaidc.Type;

public class OAIPMHMarshallTest {

	private static final JAXBContext jc;
	static {
		try {
			jc = JAXBContext.newInstance("au.com.miskinhill.schema.oaipmh:au.com.miskinhill.schema.oaidc");
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
        OaiDc oaidc = new OaiDc(
                new Title("Using Structural Metadata to Localize Experience of Digital Content"),
                new Creator("Dushay, Naomi"),
                new Subject("Digital Libraries"),
                new Description("With the increasing technical sophistication of " + 
                		"both information consumers and providers, there is " + 
                		"increasing demand for more meaningful experiences of digital " + 
                		"information. We present a framework that separates digital " + 
                		"object experience, or rendering, from digital object storage " + 
                		"and manipulation, so the rendering can be tailored to " + 
                		"particular communities of users."),
                new Description("Comment: 23 pages including 2 appendices, 8 figures"),
                new Date(new DateTime(2001, 12, 14, 1, 2, 3, 0, DateTimeZone.UTC)));
        Record record = new Record(
                new RecordHeader("oai:arXiv.org:cs/0112017",
                        new DateTime(2001, 12, 14, 1, 2, 3, 0, DateTimeZone.UTC),
                        Arrays.asList("cs", "math")),
                new Metadata(oaidc));
		OAIPMH<GetRecordResponse> oaipmh = new OAIPMH<GetRecordResponse>(
		        new DateTime(2002, 2, 8, 8, 55, 46, 0, DateTimeZone.UTC),
		        new Request(URI.create("http://arXiv.org/oai2"), Verb.GET_RECORD, "oai:arXiv.org:cs/0112017", "oai_dc"),
		        new GetRecordResponse(record));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("GetRecord-example.xml"), "UTF-8");
        assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallIdentifyResponse() throws Exception {
	    IdentifyResponse identify = new IdentifyResponse(
	            "Library of Congress Open Archive Initiative Repository 1",
	            URI.create("http://memory.loc.gov/cgi-bin/oai"),
	            new DateTime(1990, 2, 1, 12, 0, 0, 0, DateTimeZone.UTC),
	            Arrays.asList("somebody@loc.gov", "anybody@loc.gov"),
	            DeletedRecordSupport.TRANSIENT,
	            Granularity.DATE_TIME,
	            Arrays.asList("deflate"));
	    OAIPMH<IdentifyResponse> oaipmh = new OAIPMH<IdentifyResponse>(
	            new DateTime(2002, 2, 8, 12, 0, 1, 0, DateTimeZone.UTC),
	            new Request(URI.create("http://memory.loc.gov/cgi-bin/oai"), Verb.IDENTIFY),
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
	            new Request(URI.create("http://an.oa.org/OAI-script"), Verb.LIST_IDENTIFIERS,
                    new DateTime(1998, 1, 15, 0, 0, 0, 0, DateTimeZone.UTC),
                    "oldarXiv", "physics:hep"),
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
	            new Request(URI.create("http://www.perseus.tufts.edu/cgi-bin/pdataprov"), Verb.LIST_METADATA_FORMATS, "oai:perseus.tufts.edu:Perseus:text:1999.02.0119"),
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
                    new Metadata(new OaiDc(Arrays.<AbstractElement<?>>asList(
                        new Title("Opera Minora"),
                        new Creator("Cornelius Tacitus"),
                        new Type("text"),
                        new Source("Opera Minora. Cornelius Tacitus. Henry Furneaux. Clarendon Press. Oxford. 1900."),
                        new Language("latin"),
                        new Identifier("http://www.perseus.tufts.edu/cgi-bin/ptext?doc=Perseus:text:1999.02.0084"))))),
                new Record(
                    new RecordHeader("oai:perseus:Perseus:text:1999.02.0083",
                        new DateTime(2002, 5, 1, 14, 20, 55, 0, DateTimeZone.UTC)),
                    new Metadata(new OaiDc(Arrays.<AbstractElement<?>>asList(
                        new Title("Germany and its Tribes"),
                        new Creator("Tacitus"),
                        new Type("text"),
                        new Source("Complete Works of Tacitus. Tacitus. Alfred John Church. " + 
                        		"William Jackson Brodribb. Lisa Cerrato. edited for Perseus. " + 
                        		"New York: Random House, Inc. Random House, Inc. reprinted 1942."),
                        new Language("english"),
                        new Identifier("http://www.perseus.tufts.edu/cgi-bin/ptext?doc=Perseus:text:1999.02.0083")))))));
	    OAIPMH<ListRecordsResponse> oaipmh = new OAIPMH<ListRecordsResponse>(
	            new DateTime(2002, 6, 1, 19, 20, 30, 0, DateTimeZone.UTC),
	            new Request(URI.create("http://www.perseus.tufts.edu/cgi-bin/pdataprov"),
                    Verb.LIST_RECORDS,
                    new DateTime(2002, 5, 1, 14, 15, 0, 0, DateTimeZone.UTC),
                    new DateTime(2002, 5, 1, 14, 20, 0, 0, DateTimeZone.UTC),
                    "oai_dc"),
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
                    new SetDescription(new OaiDc(Arrays.<AbstractElement<?>>asList(
                        new Description("This set contains metadata describing electronic music recordings made during the 1950ies")))))),
                new Set("video", "Video Collection")));
	    OAIPMH<ListSetsResponse> oaipmh = new OAIPMH<ListSetsResponse>(
	            new DateTime(2002, 8, 11, 7, 21, 33, 0, DateTimeZone.UTC),
	            new Request(URI.create("http://an.oa.org/OAI-script"), Verb.LIST_SETS),
                listSets);
	    String expected = IOUtils.toString(this.getClass().getResourceAsStream("ListSets-example.xml"), "UTF-8");
	    assertMarshalled(expected, oaipmh);
	}
	
	@Test
	public void shouldMarshallError() throws Exception {
	    OAIPMH<ListSetsResponse> oaipmh = new OAIPMH<ListSetsResponse>(
	            new DateTime(2002, 5, 1, 9, 18, 29, 0, DateTimeZone.UTC),
	            new Request(URI.create("http://arXiv.org/oai2"), Verb.LIST_SETS),
	            Arrays.asList(new Error(ErrorCode.NO_SET_HIERARCHY, "This repository does not support sets")));
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("error-example.xml"), "UTF-8");
        assertMarshalled(expected, oaipmh);
	}

}
