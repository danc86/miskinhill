package au.com.miskinhill.web.decorator;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import au.com.miskinhill.web.util.HttpResponseBufferingFilter;

@Component("decoratingFilter")
public class DecoratingFilter extends HttpResponseBufferingFilter {
    
    private static final long serialVersionUID = -379349824773494218L;
    
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    @Override
    protected String postprocessResponse(String responseBody) {
        try {
            Transformer transformer = transformerFactory.newTransformer(
                    new SAXSource(new InputSource(this.getClass().getResourceAsStream("commonwrapper.xml"))));
            StringWriter writer = new StringWriter();
            transformer.transform(
                    new SAXSource(new InputSource(new StringReader(responseBody))),
                    new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
    
}