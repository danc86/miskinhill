package au.com.miskinhill.rdftemplate.datatype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;

public class YearUnitTest {
    
    @Test
    public void testToString() {
        assertThat(new Year(new LocalDate(2001, 1, 1)).toString(), equalTo("2001"));
    }
    
    @Test
    public void testEqualsHashCode() {
        Year year1 = new Year(new LocalDate(2001, 1, 1));
        Year year2 = new Year(new LocalDate(2001, 1, 1));
        assertThat(year1, equalTo(year2));
        assertThat(year1.hashCode(), equalTo(year2.hashCode()));
    }

}
