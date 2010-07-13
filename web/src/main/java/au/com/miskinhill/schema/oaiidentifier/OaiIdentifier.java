package au.com.miskinhill.schema.oaiidentifier;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.DOMWriter;

public class OaiIdentifier {

    private final QName XSI_SCHEMA_LOCATION_QNAME = new QName("schemaLocation", new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
    private final Namespace XML_NS = new Namespace("", "http://www.openarchives.org/OAI/2.0/oai-identifier");
    private final QName IDENTIFIER_QNAME = new QName("oai-identifier", XML_NS);
    private final QName SCHEME_QNAME = new QName("scheme", XML_NS);
    private final QName REPOSITORY_IDENTIFIER_QNAME = new QName("repositoryIdentifier", XML_NS);
    private final QName DELIMITER_QNAME = new QName("delimiter", XML_NS);
    private final QName SAMPLE_IDENTIFIER_QNAME = new QName("sampleIdentifier", XML_NS);

    private final String scheme;
    private final String repositoryIdentifier;
    private final String delimiter;
    private final String sampleIdentifier;
    
    public OaiIdentifier(String scheme, String repositoryIdentifier, String delimiter, String sampleIdentifier) {
        this.scheme = scheme;
        this.repositoryIdentifier = repositoryIdentifier;
        this.delimiter = delimiter;
        this.sampleIdentifier = sampleIdentifier;
    }

    public org.w3c.dom.Element toDom() {
        DocumentFactory f = DocumentFactory.getInstance();
        Element root = f.createElement(IDENTIFIER_QNAME)
                .addAttribute(XSI_SCHEMA_LOCATION_QNAME,
                        "http://www.openarchives.org/OAI/2.0/oai-identifier " +
                        "http://www.openarchives.org/OAI/2.0/oai-identifier.xsd");
        root.addElement(SCHEME_QNAME).addText(scheme);
        root.addElement(REPOSITORY_IDENTIFIER_QNAME).addText(repositoryIdentifier);
        root.addElement(DELIMITER_QNAME).addText(delimiter);
        root.addElement(SAMPLE_IDENTIFIER_QNAME).addText(sampleIdentifier);
        Document doc = f.createDocument(root);
        try {
            return new DOMWriter().write(doc).getDocumentElement();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

}
