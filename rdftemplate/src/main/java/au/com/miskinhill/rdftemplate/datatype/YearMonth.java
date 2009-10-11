package au.com.miskinhill.rdftemplate.datatype;

import org.joda.time.LocalDate;

public class YearMonth {
    
    private final int year;
    private final int month;
    
    public YearMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }
    
    public YearMonth(LocalDate date) {
        this.year = date.getYear();
        this.month = date.getMonthOfYear();
    }
    
    public int getYear() {
        return year;
    }
    
    public int getMonth() {
        return month;
    }
    
    @Override
    public String toString() {
        return String.format("%04d-%02d", year, month);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + month;
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
        YearMonth other = (YearMonth) obj;
        if (month != other.month)
            return false;
        if (year != other.year)
            return false;
        return true;
    }

}
