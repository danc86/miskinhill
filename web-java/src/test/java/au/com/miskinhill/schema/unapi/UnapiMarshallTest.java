package au.com.miskinhill.schema.unapi;

import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class UnapiMarshallTest {

	private static final JAXBContext jc;
	static {
		try {
			jc = JAXBContext.newInstance("au.com.miskinhill.schema.unapi");
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void shouldMarshall() throws Exception {
	    Formats formats = Formats.forId("http://example.com/asdf");
	    formats.add(new Format("html", MediaType.TEXT_HTML_TYPE, "http://www.w3.org/TR/xhtml1/"));
	    formats.add(new Format("text", MediaType.TEXT_PLAIN_TYPE));
		
		Marshaller m = jc.createMarshaller();
		StringWriter w = new StringWriter();
		m.marshal(formats, w);
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<formats id=\"http://example.com/asdf\">" +
				    "<format name=\"html\" type=\"text/html\" docs=\"http://www.w3.org/TR/xhtml1/\"/>" +
				    "<format name=\"text\" type=\"text/plain\"/>" +
				"</formats>",
				w.toString());
	}

}
