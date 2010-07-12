package au.com.miskinhill.web.oaipmh;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;
import org.w3c.dom.Element;

import au.id.djc.rdftemplate.XMLStream;

public class OaipmhControllerUnitTest {
    
    private static final QName OAI_DC = new QName("http://www.openarchives.org/OAI/2.0/oai_dc/", "dc", "oai_dc");
    private static final QName DC_TITLE = new QName("http://purl.org/dc/elements/1.1/", "title", "dc");
    
    @Test
    public void testDomElementFromStream() throws Exception {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        ArrayList<XMLEvent> events = new ArrayList<XMLEvent>();
        events.add(eventFactory.createStartDocument());
        events.add(eventFactory.createStartElement(OAI_DC, null, null));
        events.add(eventFactory.createStartElement(DC_TITLE, null, null));
        events.add(eventFactory.createCharacters("lol"));
        events.add(eventFactory.createEndElement(DC_TITLE, null));
        events.add(eventFactory.createEndElement(OAI_DC, null));
        events.add(eventFactory.createEndDocument());
        
        OaipmhController controller = new OaipmhController(null, null, null, XMLOutputFactory.newInstance());
        Element element = controller.domElementFromStream(new XMLStream(events));
        assertThat(element.getChildNodes().getLength(), equalTo(1));
    }

}
