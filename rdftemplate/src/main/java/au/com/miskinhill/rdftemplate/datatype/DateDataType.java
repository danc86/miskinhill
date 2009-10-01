package au.com.miskinhill.rdftemplate.datatype;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Component
public class DateDataType implements RDFDatatype {
    
    public static final String URI = "http://www.w3.org/TR/xmlschema-2/#date";
    
    @SuppressWarnings("unused")
    private static DateDataType instance;
    public static void registerStaticInstance() {
        instance = new DateDataType();
    }
    
    private final DateTimeFormatter yearParser = DateTimeFormat.forPattern("yyyy");
    private final DateTimeFormatter yearMonthParser = DateTimeFormat.forPattern("yyyy-MM");
    private final DateTimeFormatter dateParser = DateTimeFormat.forPattern("yyyy-MM-dd");

    public DateDataType() {
        TypeMapper.getInstance().registerDatatype(this);
    }

    @Override
    public String getURI() {
        return URI;
    }
    
    @Override
    public Class<LocalDate> getJavaClass() {
        return null;
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
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return dateParser.parseDateTime(lexicalForm).toLocalDate();
        } catch (IllegalArgumentException e) {
            // pass
        }
        try {
            return new YearMonth(yearMonthParser.parseDateTime(lexicalForm).toLocalDate());
        } catch (IllegalArgumentException e) {
            // pass
        }
        try {
            return new Year(yearParser.parseDateTime(lexicalForm).toLocalDate());
        } catch (IllegalArgumentException e) {
            // pass
        }
        throw new DatatypeFormatException(lexicalForm, this, "No matching parsers found");
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
        return (valueForm instanceof LocalDate);
    }

    @Override
    public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
        return dt;
    }

}