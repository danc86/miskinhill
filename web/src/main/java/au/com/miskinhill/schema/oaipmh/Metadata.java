package au.com.miskinhill.schema.oaipmh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.id.djc.rdftemplate.XMLStream;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Metadata {
    
    private static final DocumentBuilder DOCUMENT_BUILDER;
    private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    static {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DOCUMENT_BUILDER = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @XmlAnyElement(lax = true)
    private Object any;
    
    protected Metadata() {
    }
    
    public Metadata(Object any) {
        this.any = any;
    }
    
    public Metadata(XMLStream stream) {
        try {
            this.any = domElementFromStream(stream);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object getAny() {
        return any;
    }
    
    private Element domElementFromStream(XMLStream stream) throws XMLStreamException {
        Document document = DOCUMENT_BUILDER.newDocument();
        DOMResult result = new DOMResult(document);
        XMLEventWriter eventWriter = OUTPUT_FACTORY.createXMLEventWriter(result);
        long depth = 0;
        for (XMLEvent event: stream) {
            // DOM stuff explodes if we write characters outside the root element,
            // so have to filter those out
            switch (event.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                    if (depth > 0)
                        eventWriter.add(event);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    depth ++;
                    eventWriter.add(event);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth --;
                    eventWriter.add(event);
                    break;
                default:
                    eventWriter.add(event);
            }
        }
        return document.getDocumentElement();
    }

}
