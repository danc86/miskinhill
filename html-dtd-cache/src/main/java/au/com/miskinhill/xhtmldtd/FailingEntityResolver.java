package au.com.miskinhill.xhtmldtd;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FailingEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        throw new IllegalStateException("Entity should have been resolved from the cache: "+systemId);
    }

}
