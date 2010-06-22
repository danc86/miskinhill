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
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

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
import au.com.miskinhill.rdf.RDFUtil;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdf.vocabulary.NamespacePrefixMapper;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

public class Preprocessor {
    
    private static final Logger LOG = Logger.getLogger(Preprocessor.class.getName());
    private static final XPath A_XPATH = DocumentFactory.getInstance().createXPath("//html:a");
    static {
        A_XPATH.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
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
        Properties paths = new Properties();
        paths.load(Preprocessor.class.getResourceAsStream("paths.properties"));
        File contentRoot = new File(paths.getProperty("contentPath"));
        
        JenaParameters.enableEagerLiteralValidation = true;
        Model model = load(contentRoot, Collections.singleton("thirdparty"));
        extractResponsibility(model);
        extractCitations(model, contentRoot);
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
                                m.add(m.createStatement(stmt.getSubject(), m.createProperty(DCTerms.NS + "creator"), href));
                            else
                                throw new AssertionError("Unknown responsibility rel value [" + rel + "]");
                        }
                    }
                }
            }
        }
        LOG.info("Model contains " + m.getGraph().size() + " triples");
    }
    
    private static void extractCitations(Model m, File contentRoot) throws XMLStreamException, IOException {
        LOG.info("Extracting citation metadata");
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
                for (Citation citation: Citation.fromDocument(URI.create(item.getURI()), contentReader))
                    for (Statement stmt: citation.toRDF())
                        m.add(stmt);
            }
        }
        LOG.info("Model contains " + m.getGraph().size() + " triples");
    }
    
    private static void writeResult(Model model, Model inferredModel) throws IOException {
        File modelFile = new File("meta.xml");
        LOG.info("Writing model to " + modelFile.getAbsolutePath());
        model.write(new BufferedOutputStream(new FileOutputStream(modelFile)), "RDF/XML");
        
        File inferredModelFile = new File("meta-inferred.xml");
        LOG.info("Writing inferred model to " + inferredModelFile.getAbsolutePath());
        inferredModel.write(new BufferedOutputStream(new FileOutputStream(inferredModelFile)), "RDF/XML");
    }
    
}
