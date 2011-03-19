package au.com.miskinhill.search.analysis;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;
import org.apache.lucene.analysis.TokenStream;

public abstract class RDFLiteralTokenizer extends TokenStream {

    private static final String XML_LITERAL = (RDF.getURI() + "XMLLiteral").intern();
    private static final String XSD_DATE = "http://www.w3.org/TR/xmlschema-2/#date";
    private static final String XSD_INTEGER = XSD.integer.getURI().intern();

    public static class UnknownLiteralTypeException extends Exception {
        private static final long serialVersionUID = 3417574009217953585L;

        public UnknownLiteralTypeException(String type) {
            super("Could not resolve literal type <" + type
                    + "> to an RDFLiteralTokenizer subclass");
        }
    }

    public static TokenStream fromLiteral(Literal literal) 
            throws UnknownLiteralTypeException, XMLStreamException {
        String type = literal.getDatatypeURI();
        if (type != null) type = type.intern();
        if (type == null /* no type means plain string */ ||
                XSD_DATE == type /* XXX use something better? */) {
            Analyzer analyzer = MHAnalyzers.getAnalyzerMap().getAnalyzer(literal.getLanguage());
            return analyzer.applyFilters(analyzer.tokenizer(
                    new StringReader(literal.getString())));
        } else if (XSD_INTEGER == type) {
            return new IntegerLiteralTokenStream(literal);
        } else if (XML_LITERAL == type) {
            return new XMLTokenizer(new StringReader(literal.getString()),
                    MHAnalyzers.getAnalyzerMap());
        } else {
            throw new UnknownLiteralTypeException(type);
        }
    }

}