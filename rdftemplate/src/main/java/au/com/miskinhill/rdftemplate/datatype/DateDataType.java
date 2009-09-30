package au.com.miskinhill.rdftemplate.datatype;

import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateDataType implements RDFDatatype {
    
    public static final String URI = "http://www.w3.org/TR/xmlschema-2/#date";
    
    public static final DateDataType instance = new DateDataType();
    public static void register() {
        TypeMapper.getInstance().registerDatatype(instance);
    }
    
    private final List<DateTimeFormatter> parsers = Arrays.asList(
            DateTimeFormat.forPattern("yyyy"),
            DateTimeFormat.forPattern("yyyy-mm"),
            DateTimeFormat.forPattern("yyyy-mm-dd"));

    private DateDataType() {
    }

    @Override
    public String getURI() {
        return URI;
    }
    
    @Override
    public Class<LocalDate> getJavaClass() {
        return LocalDate.class;
    }

    @Override
    public String unparse(Object value) {
        LocalDate date = (LocalDate) value;
        return date.toString();
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
    public LocalDate parse(String lexicalForm) throws DatatypeFormatException {
        for (DateTimeFormatter parser: parsers) {
            try {
                return parser.parseDateTime(lexicalForm).toLocalDate();
            } catch (IllegalArgumentException e) {
                // pass
            }
        }
        throw new DatatypeFormatException(lexicalForm, this, "No matching parsers found");
    }

    @Override
    public boolean isValid(String lexicalForm) {
        for (DateTimeFormatter parser: parsers) {
            try {
                parser.parseDateTime(lexicalForm);
                return true;
            } catch (IllegalArgumentException e) {
                // pass
            }
        }
        return false;
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