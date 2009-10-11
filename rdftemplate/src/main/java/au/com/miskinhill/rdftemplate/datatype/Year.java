package au.com.miskinhill.rdftemplate.datatype;

import org.joda.time.LocalDate;

public class Year {
    
    private final int year;
    
    public Year(int value) {
        this.year = value;
    }
    
    public Year(LocalDate date) {
        this.year = date.getYear();
    }
    
    public Year(YearMonth yearMonth) {
        this.year = yearMonth.getYear();
    }
    
    public int getYear() {
        return year;
    }
    
    @Override
    public String toString() {
        return Integer.toString(year);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Year other = (Year) obj;
        return (year == other.year);
    }

}
