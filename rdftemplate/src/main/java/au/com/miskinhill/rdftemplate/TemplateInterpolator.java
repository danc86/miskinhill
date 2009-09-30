package au.com.miskinhill.rdftemplate;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.commons.lang.StringUtils;

import au.com.miskinhill.rdftemplate.selector.Selector;
import au.com.miskinhill.rdftemplate.selector.SelectorParser;

public class TemplateInterpolator {
    
    public static final String NS = "http://code.miskinhill.com.au/rdftemplate/";
    public static final String CONTENT_ACTION = "content";
    private static final QName CONTENT_ACTION_QNAME = new QName(NS, CONTENT_ACTION);
    public static final String FOR_ACTION = "for";
    private static final QName FOR_ACTION_QNAME = new QName(NS, FOR_ACTION);
    private static final QName XML_LANG_QNAME = new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX);
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    static {
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);
    }
    
    private TemplateInterpolator() {
    }
    
    @SuppressWarnings("unchecked")
    public static String interpolate(Reader reader, RDFNode node) throws XMLStreamException {
        StringWriter writer = new StringWriter();
        interpolate(inputFactory.createXMLEventReader(reader), node, outputFactory.createXMLEventWriter(writer));
        return writer.toString();
    }
    
    public static void interpolate(Iterator<XMLEvent> reader, RDFNode node, XMLEventWriter writer)
            throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.next();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT: {
                    StartElement start = (StartElement) event;
                    Attribute contentAttribute = start.getAttributeByName(CONTENT_ACTION_QNAME);
                    Attribute forAttribute = start.getAttributeByName(FOR_ACTION_QNAME);
                    if (contentAttribute != null && forAttribute != null) {
                        throw new TemplateSyntaxException("rdf:for and rdf:content cannot both be present on an element");
                    } else if (contentAttribute != null) {
                        consumeTree(start, reader);
                        start = interpolateAttributes(start, node);
                        Selector<?> selector = SelectorParser.parse(contentAttribute.getValue());
                        writeTreeForContent(writer, start, selector.singleResult(node));
                    } else if (forAttribute != null) {
                        start = cloneStartWithAttributes(start, cloneAttributesWithout(start, FOR_ACTION_QNAME));
                        List<XMLEvent> tree = consumeTree(start, reader);
                        Selector<RDFNode> selector = SelectorParser.parse(forAttribute.getValue()).withResultType(RDFNode.class);
                        for (RDFNode subNode : selector.result(node)) {
                            interpolate(tree.iterator(), subNode, writer);
                        }
                    } else {
                        start = interpolateAttributes(start, node);
                        writer.add(start);
                    }
                    break;
                }
                case XMLStreamConstants.CHARACTERS: {
                    Characters characters = (Characters) event;
                    writer.add(eventFactory.createCharacters(interpolateString(characters.getData(), node)));
                    break;
                }
                case XMLStreamConstants.CDATA: {
                    Characters characters = (Characters) event;
                    writer.add(eventFactory.createCData(interpolateString(characters.getData(), node)));
                    break;
                }
                default:
                    writer.add(event);
            }
        }
    }
    
    private static List<XMLEvent> consumeTree(StartElement start, Iterator<XMLEvent> reader) throws XMLStreamException {
        List<XMLEvent> events = new ArrayList<XMLEvent>();
        events.add(start);
        Deque<QName> elementStack = new LinkedList<QName>();
        while (reader.hasNext()) {
            XMLEvent event = reader.next();
            events.add(event);
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    elementStack.addLast(((StartElement) event).getName());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (elementStack.isEmpty()) {
                        return events;
                    } else {
                        if (!elementStack.removeLast().equals(((EndElement) event).getName()))
                            throw new IllegalStateException("End element mismatch");
                    }
                    break;
                default:
            }
        }
        throw new IllegalStateException("Reader exhausted before end element found");
    }
    
    @SuppressWarnings("unchecked")
    private static StartElement interpolateAttributes(StartElement start, RDFNode node) {
        Set<Attribute> replacementAttributes = new LinkedHashSet<Attribute>();
        for (Iterator<Attribute> it = start.getAttributes(); it.hasNext(); ) {
            Attribute attribute = it.next();
            String replacementValue = attribute.getValue();
            if (!attribute.getName().getNamespaceURI().equals(NS)) // skip rdf: attributes
                replacementValue = interpolateString(attribute.getValue(), node); 
            replacementAttributes.add(eventFactory.createAttribute(attribute.getName(),
                    replacementValue));
        }
        return cloneStartWithAttributes(start, replacementAttributes);
    }
    
    private static StartElement cloneStartWithAttributes(StartElement start, Iterable<Attribute> attributes) {
        return eventFactory.createStartElement(
                start.getName().getPrefix(),
                start.getName().getNamespaceURI(),
                start.getName().getLocalPart(),
                attributes.iterator(),
                start.getNamespaces(),
                start.getNamespaceContext());
    }
    
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");
    public static String interpolateString(String template, RDFNode node) {
        if (!SUBSTITUTION_PATTERN.matcher(template).find()) {
            return template; // fast path
        }
        StringBuffer substituted = new StringBuffer();
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(template);
        while (matcher.find()) {
            String expression = matcher.group(1);
            Object replacement = SelectorParser.parse(expression).singleResult(node);
            
            String replacementValue;
            if (replacement instanceof RDFNode) {
                RDFNode replacementNode = (RDFNode) replacement;
                if (replacementNode.isLiteral()) {
                    Literal replacementLiteral = (Literal) replacementNode;
                    replacementValue = replacementLiteral.getValue().toString();
                } else {
                    throw new UnsupportedOperationException("Not a literal: " + replacementNode);
                }
            } else if (replacement instanceof String) {
                replacementValue = (String) replacement;
            } else {
                throw new UnsupportedOperationException("Not an RDFNode: " + replacement);
            }
            
            matcher.appendReplacement(substituted, replacementValue.replace("$", "\\$"));;
        }
        matcher.appendTail(substituted);
        return substituted.toString();
    }
    
    private static void writeTreeForContent(XMLEventWriter writer, StartElement start, Object replacement)
            throws XMLStreamException {
        if (replacement instanceof RDFNode) {
            RDFNode replacementNode = (RDFNode) replacement;
            if (replacementNode.isLiteral()) {
                Literal literal = (Literal) replacementNode;
                Set<Attribute> attributes = cloneAttributesWithout(start, CONTENT_ACTION_QNAME);
                
                if (!StringUtils.isEmpty(literal.getLanguage())) {
                    attributes.add(eventFactory.createAttribute(XML_LANG_QNAME, literal.getLanguage()));
                    if (start.getName().getNamespaceURI().equals(XHTML_NS_URI)) {
                        String xhtmlPrefixInContext = start.getNamespaceContext().getPrefix(XHTML_NS_URI);
                        QName xhtmlLangQNameForContext; // ugh
                        if (xhtmlPrefixInContext.isEmpty())
                            xhtmlLangQNameForContext = new QName("lang");
                        else
                            xhtmlLangQNameForContext = new QName(XHTML_NS_URI, "lang", xhtmlPrefixInContext);
                        attributes.add(eventFactory.createAttribute(xhtmlLangQNameForContext, literal.getLanguage()));
                    }
                }
                
                writer.add(eventFactory.createStartElement(start.getName(), attributes.iterator(), start.getNamespaces()));
                if (literal.isWellFormedXML()) {
                    writeXMLLiteral(start.getNamespaceContext(), literal.getLexicalForm(), writer);
                } else {
                    writer.add(eventFactory.createCharacters(literal.getValue().toString()));
                }
                writer.add(eventFactory.createEndElement(start.getName(), start.getNamespaces()));
            } else {
                throw new UnsupportedOperationException("Not a literal: " + replacementNode);
            }
        } else {
            throw new UnsupportedOperationException("Not an RDFNode: " + replacement);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Attribute> cloneAttributesWithout(StartElement start, QName omit) {
        // clone attributes, but without rdf:content
        Set<Attribute> attributes = new LinkedHashSet<Attribute>();
        for (Iterator<Attribute> it = start.getAttributes(); it.hasNext(); ) {
            Attribute attribute = it.next();
            if (!attribute.getName().equals(omit))
                attributes.add(attribute);
        }
        return attributes;
    }
    
    private static void writeXMLLiteral(NamespaceContext nsContext, String literal, XMLEventWriter writer)
            throws XMLStreamException {
        XMLEventReader reader = inputFactory.createXMLEventReader(new StringReader(literal));
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                case XMLStreamConstants.END_DOCUMENT:
                    break; // discard
                default:
                    writer.add(event);
            }
        }
    }
    
}
