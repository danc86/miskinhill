package au.com.miskinhill.rdftemplate.datatype;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

@Component
public class DateTimeDataType implements RDFDatatype {
    
    public static final String URI = "http://www.w3.org/TR/xmlschema-2/#datetime";
    
    @SuppressWarnings("unused")
    private static DateTimeDataType instance;
    public static void registerStaticInstance() {
        instance = new DateTimeDataType();
    }
    
    private final DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();

    public DateTimeDataType() {
        TypeMapper.getInstance().registerDatatype(this);
    }

    @Override
    public String getURI() {
        return URI;
    }
    
    @Override
    public Class<DateTime> getJavaClass() {
        return DateTime.class;
    }

    @Override
    public String unparse(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object cannonicalise(Object value) {
        return value;
    }

    @Override
    public Object extendedTypeDefinition() {
        return null;
    }

    @Override
    public int getHashCode(LiteralLabel lit) {
        return lit.getValue().hashCode();
    }

    @Override
    public boolean isEqual(LiteralLabel left, LiteralLabel right) {
        return left.getValue().equals(right.getValue());
    }
    
    @Override
    public DateTime parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return parser.parseDateTime(lexicalForm);
        } catch (IllegalArgumentException e) {
            throw new DatatypeFormatException(lexicalForm, this, "Parser barfed");
        }
    }

    @Override
    public boolean isValid(String lexicalForm) {
        try {
            parse(lexicalForm);
            return true;
        } catch (DatatypeFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        return lit.getDatatypeURI().equals(URI) && isValid(lit.getLexicalForm());
    }

    @Override
    public boolean isValidValue(Object valueForm) {
        return (valueForm instanceof DateTime);
    }

    @Override
    public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
        return dt;
    }

}