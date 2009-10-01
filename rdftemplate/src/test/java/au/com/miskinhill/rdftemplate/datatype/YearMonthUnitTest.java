package au.com.miskinhill.rdftemplate.datatype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;

public class YearMonthUnitTest {
    
    @Test
    public void testToString() {
        assertThat(new YearMonth(new LocalDate(2001, 5, 1)).toString(), equalTo("2001-05"));
    }
    
    @Test
    public void testEqualsHashCode() {
        YearMonth yearMonth1 = new YearMonth(new LocalDate(2001, 5, 1));
        YearMonth yearMonth2 = new YearMonth(new LocalDate(2001, 5, 1));
        assertThat(yearMonth1, equalTo(yearMonth2));
        assertThat(yearMonth1.hashCode(), equalTo(yearMonth2.hashCode()));
    }

}
