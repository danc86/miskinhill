package au.com.miskinhill.preprocessor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import au.com.miskinhill.citation.Citation;
import au.com.miskinhill.rdf.LanguageUtil;
import au.com.miskinhill.rdf.RDFUtil;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdf.vocabulary.NamespacePrefixMapper;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class Preprocessor {
    
    private static final Logger LOG = Logger.getLogger(Preprocessor.class.getName());
    private static final String XHTML_NS_URI = "http://www.w3.org/1999/xhtml";
    private static final XPath A_XPATH = DocumentFactory.getInstance().createXPath("//html:a");
    static {
        A_XPATH.setNamespaceURIs(Collections.singletonMap("html", XHTML_NS_URI));
    }
    private static final byte[] XHTML_STRICT_DTD_DECL = 
            ("<!DOCTYPE html " +
            "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
            "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
            .getBytes();
    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    static {
        inputFactory.setXMLResolver(new XhtmlEntityResolver());
    }
    
    public static void main(String[] args) throws Exception {
        File contentRoot = new File(System.getProperty("au.com.miskinhill.contentPath"));
        
        JenaParameters.enableEagerLiteralValidation = true;
        Model model = load(contentRoot, Collections.singleton("thirdparty"));
        extractResponsibility(model);
        processContent(model, contentRoot);
        Model inferredModel = load(new File(contentRoot, "thirdparty"), Collections.<String>emptySet());
        Model union = ModelFactory.createUnion(model, inferredModel);
        new Inferrer(union).apply(inferredModel);
        new Validator(union).validate();
        writeResult(model, inferredModel);
    }
    
    private static Model load(File base, Set<String> excludes) throws IOException {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes(NamespacePrefixMapper.getInstance());
        Queue<File> queue = new LinkedList<File>();
        queue.add(base);
        while (!queue.isEmpty()) {
            for (File child: queue.remove().listFiles()) {
                if (child.isDirectory() && !excludes.contains(child.getName())) {
                    queue.add(child);
                } else if (child.isFile()) {
                    if (child.getName().endsWith(".nt")) {
                        LOG.info("Loading NTriples from " + child );
                        m.read(new FileInputStream(child), "", "N-TRIPLE");
                        LOG.info("Model contains " + m.getGraph().size() + " triples");
                    }
                    if (child.getName().endsWith(".ttl")) {
                        LOG.info("Loading Turtle from " + child);
                        m.read(new FileInputStream(child), "", "TURTLE");
                        LOG.info("Model contains " + m.getGraph().size() + " triples");
                    }
                }
            }
        }
        return m;
    }
    
    @SuppressWarnings("unchecked")
    private static void extractResponsibility(Model m) throws DocumentException {
        LOG.info("Extracting responsible roles");
        Iterator<Statement> it = m.listStatements(null, MHS.responsibility, (RDFNode) null)
                .andThen(m.listStatements(null, m.createProperty(DCTerms.NS + "title"), (RDFNode) null));
        while (it.hasNext()) {
            Statement stmt = it.next();
            if (!stmt.getObject().isLiteral())
                continue;
            Literal object = (Literal) stmt.getObject();
            if (object.getDatatype() == null || !object.getDatatype().equals(XMLLiteralType.theXMLLiteralType))
                continue;
            String xml = ((Literal) stmt.getObject()).getLexicalForm();
            Document doc = DocumentHelper.parseText(xml);
            for (Element anchor: (List<Element>) A_XPATH.selectNodes(doc)) {
                Resource href = m.createResource(anchor.attributeValue("href"));
                if (RDFUtil.getTypes(href).contains(MHS.Author)) {
                    String relValue = anchor.attributeValue("rel");
                    if (relValue == null)
                        m.add(m.createStatement(stmt.getSubject(), m.createProperty(DCTerms.NS + "creator"), href));
                    else {
                        Set<String> rels = new HashSet<String>(Arrays.asList(relValue.split("\\s")));
                        for (String rel: rels) {
                            if (rel.equals("contributor"))
                                m.add(m.createStatement(stmt.getSubject(), m.createProperty(DCTerms.NS + "contributor"), href));
                            else if (rel.equals("translator"))
                                m.add(m.createStatement(stmt.getSubject(), MHS.translator, href));
                            else if (rel.equals("editor"))
                                m.add(m.createStatement(stmt.getSubject(), MHS.editor, href));
                            else
                                throw new AssertionError("Unknown responsibility rel value [" + rel + "]");
                        }
                    }
                }
            }
        }
        LOG.info("Model contains " + m.getGraph().size() + " triples");
    }
    
    private static void processContent(Model m, File contentRoot) throws XMLStreamException, IOException {
        LOG.info("Processing content");
        Iterator<Resource> it = m.listSubjectsWithProperty(RDF.type, MHS.Article)
                .andThen(m.listSubjectsWithProperty(RDF.type, MHS.Review))
                .andThen(m.listSubjectsWithProperty(RDF.type, MHS.Obituary));
        while (it.hasNext()) {
            Resource item = it.next();
            if (item.getURI().startsWith("http://miskinhill.com.au/journals/")) {
                File content = new File(contentRoot, item.getURI().substring("http://miskinhill.com.au/".length()) + ".html");
                if (!content.isFile()) {
                    LOG.warning("Skipping non-existent content " + content);
                    continue;
                }
                XMLEventReader contentReader = inputFactory.createXMLEventReader(new SequenceInputStream(
                        new ByteArrayInputStream(XHTML_STRICT_DTD_DECL),
                        new FileInputStream(content)));
                extractCitations(m, item, contentReader);
                contentReader = inputFactory.createXMLEventReader(new SequenceInputStream(
                        new ByteArrayInputStream(XHTML_STRICT_DTD_DECL),
                        new FileInputStream(content)));
                extractLanguages(m, item, contentReader);
            }
        }
        LOG.info("Model contains " + m.getGraph().size() + " triples");
    }

    private static void extractCitations(Model model, Resource item, XMLEventReader content) {
        for (Citation citation: Citation.fromDocument(URI.create(item.getURI()), content))
            for (Statement stmt: citation.toRDF())
                model.add(stmt);
    }
    
    private static void extractLanguages(Model model, Resource item, XMLEventReader content) throws XMLStreamException {
        while (content.hasNext()) {
            XMLEvent event = content.nextEvent();
            switch (event.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String lang = getLang(startElement);
                    if (lang == null)
                        throw new AssertionError("lang attribute missing from root element of " + item);
                    model.add(item, DCTerms.language, LanguageUtil.lookupLanguage(model, lang));
                    return;
                default:
                    // continue
            }
        }
        throw new AssertionError("No start element found for " + item);
    }
    
    private static void writeResult(Model model, Model inferredModel) throws IOException {
        File modelFile = new File("meta.xml");
        LOG.info("Writing model to " + modelFile.getAbsolutePath());
        model.write(new BufferedOutputStream(new FileOutputStream(modelFile)), "RDF/XML");
        
        File inferredModelFile = new File("meta-inferred.xml");
        LOG.info("Writing inferred model to " + inferredModelFile.getAbsolutePath());
        inferredModel.write(new BufferedOutputStream(new FileOutputStream(inferredModelFile)), "RDF/XML");
    }
    
    // ugh
    private static String getLang(StartElement se) {
        // xml:lang takes precedence
        QName xmlLangQName = new QName(
                se.getNamespaceURI("") == XMLConstants.XML_NS_URI ? "" : XMLConstants.XML_NS_URI, "lang");
        Attribute xmlLang = se.getAttributeByName(xmlLangQName);
        if (xmlLang != null)
            return xmlLang.getValue();

        QName xhtmlLangQName = new QName(se.getNamespaceURI("") == XHTML_NS_URI ? "" : XHTML_NS_URI, "lang");
        Attribute xhtmlLang = se.getAttributeByName(xhtmlLangQName);
        if (xhtmlLang != null)
            return xhtmlLang.getValue();

        return null;
    }
    
}
