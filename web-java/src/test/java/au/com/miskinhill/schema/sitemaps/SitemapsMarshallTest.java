package au.com.miskinhill.schema.sitemaps;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.StringWriter;
import java.math.BigDecimal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class SitemapsMarshallTest {

	private static JAXBContext jc;
	static {
		try {
			jc = JAXBContext.newInstance("au.com.miskinhill.web.sitemaps");
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void shouldMarshall() throws Exception {
		Urlset urlset = new Urlset();
		urlset.getUrls().add(new Url("http://example.com/asdf"));
		urlset.getUrls().add(new Url("http://example.com/another", "2009-09-14",
				ChangeFreq.MONTHLY, BigDecimal.ONE));
		
		Marshaller m = jc.createMarshaller();
		StringWriter w = new StringWriter();
		m.marshal(urlset, w);
		assertThat(w.toString(), equalTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">" +
				"<url><loc>http://example.com/asdf</loc></url>" +
				"<url><loc>http://example.com/another</loc>" +
					"<lastmod>2009-09-14</lastmod>" +
					"<changefreq>MONTHLY</changefreq>" +
					"<priority>1</priority>" +
				"</url></urlset>"));
	}

}
