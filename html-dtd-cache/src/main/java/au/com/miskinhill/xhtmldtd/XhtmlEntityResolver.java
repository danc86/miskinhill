package au.com.miskinhill.xhtmldtd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XhtmlEntityResolver implements EntityResolver, XMLResolver {
    static final String XHTML_PREFIX="http://www.w3.org/TR/xhtml1/DTD/";

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if(systemId.startsWith(XHTML_PREFIX)) {
            String name = systemId.substring(XHTML_PREFIX.length());
			//final InputStream resourceAsStream = getClass().getResourceAsStream(name);
            URL resource = getClass().getResource(name);
            if(resource != null) {
				InputSource inputSource = new InputSource(resource.toExternalForm());
                inputSource.setPublicId(publicId);
                //inputSource.setByteStream(resourceAsStream);
                return inputSource;
            }
        }
        
        // Let file: URLs just get loaded using the default mechanism
        if(systemId.startsWith("file:") || systemId.startsWith("jar:")) {
        	return null;
        }
        throw new IllegalStateException("Entity should have been resolved from the cache");
    }

    @Override
    public Object resolveEntity(String publicId, String systemId, String baseURI, String namespace)
            throws XMLStreamException {
        if (systemId.startsWith(XHTML_PREFIX)) {
            String name = systemId.substring(XHTML_PREFIX.length());
            InputStream resourceAsStream = getClass().getResourceAsStream(name);
            if (resourceAsStream != null) {
                return resourceAsStream;
            }
        }
        
        // Let file: URLs just get loaded using the default mechanism
        if (systemId.startsWith("file:") || systemId.startsWith("jar:")) {
            return null;
        }
        
        // stupid fucking hack because I don't have a real catalog resolver or whatever, blerg
        if (systemId.startsWith("xhtml")) {
            // means it's just an unresolved relative URL that is referenced from a DTD
            InputStream resourceAsStream = getClass().getResourceAsStream(systemId);
            if (resourceAsStream != null) {
                return resourceAsStream;
            }
        }
        
        throw new IllegalStateException("Entity should have been resolved from the cache");
    }
}
