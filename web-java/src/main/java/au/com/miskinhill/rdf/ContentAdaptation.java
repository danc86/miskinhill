package au.com.miskinhill.rdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.domain.fulltext.FulltextFetcher;
import au.com.miskinhill.rdftemplate.XMLStream;
import au.com.miskinhill.rdftemplate.selector.Adaptation;
import au.com.miskinhill.rdftemplate.selector.SelectorEvaluationException;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class ContentAdaptation implements Adaptation<XMLStream> {
    
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static final byte[] XHTML_STRICT_DTD_DECL = 
            ("<!DOCTYPE html " +
            "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n").getBytes();
    static {
        inputFactory.setXMLResolver(new XhtmlEntityResolver());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public XMLStream adapt(RDFNode node) {
        Resource resource = node.as(Resource.class);
        if (!resource.getURI().startsWith("http://miskinhill.com.au/"))
            throw new SelectorEvaluationException("Attempted to apply #content to non-Miskin-Hill node " + resource);
        try {
            String pathToContent = resource.getURI().substring(25) + ".html";
            InputStream fulltext = new SequenceInputStream(
                    new ByteArrayInputStream(XHTML_STRICT_DTD_DECL),
                    StaticApplicationContextAccessor.getBeanOfType(FulltextFetcher.class)
                        .fetchFulltext(pathToContent));
            List<XMLEvent> events = new ArrayList<XMLEvent>();
            for (Iterator<XMLEvent> reader = inputFactory.createXMLEventReader(fulltext); reader.hasNext(); ) {
                XMLEvent event = reader.next();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                    case XMLStreamConstants.END_DOCUMENT:
                    case XMLStreamConstants.DTD:
                        // discard
                        break;
                    default:
                        events.add(event);
                }
            }
            return new XMLStream(events);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<XMLStream> getDestinationType() {
        return XMLStream.class;
    }

}
