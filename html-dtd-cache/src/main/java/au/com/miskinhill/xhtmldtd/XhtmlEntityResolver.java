package au.com.miskinhill.xhtmldtd;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XhtmlEntityResolver implements EntityResolver {
    static final String XHTML_PREFIX="http://www.w3.org/TR/xhtml1/DTD/";
    final EntityResolver next;
    
    public XhtmlEntityResolver(EntityResolver next) {
        this.next = next;
    }

    public XhtmlEntityResolver() {
        this(null);
    }

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
        if(next != null)
            return next.resolveEntity(publicId, systemId);
        else
            return null;
    }
}
