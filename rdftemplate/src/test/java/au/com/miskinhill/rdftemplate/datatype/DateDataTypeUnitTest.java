package au.com.miskinhill.rdftemplate.datatype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

public class DateDataTypeUnitTest {
    
    private RDFDatatype type;
    
    @Before
    public void setUp() {
        type = new DateDataType();
    }
    
    @Test
    public void shouldParseYear() {
        assertThat((Year) type.parse("2003"), equalTo(new Year(2003)));
    }
    
    @Test
    public void shouldParseYearMonth() {
        assertThat((YearMonth) type.parse("2003-05"), equalTo(new YearMonth(2003, 5)));
    }
    
    @Test
    public void shouldParseDate() {
        assertThat((LocalDate) type.parse("2003-05-25"), equalTo(new LocalDate(2003, 5, 25)));
    }

}
