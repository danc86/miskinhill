package au.com.miskinhill.web.exception;

import org.springframework.http.HttpStatus;

public class RedirectException extends WebApplicationException {

    private static final long serialVersionUID = 2867168264798481395L;
    
    private final String location;
    
    public RedirectException(String location) {
        super(HttpStatus.FOUND);
        this.location = location;
    }
    
    public RedirectException(String location, HttpStatus redirectStatus) {
        super(redirectStatus);
        this.location = location;
    }
    
    public String getLocation() {
        return location;
    }

}
