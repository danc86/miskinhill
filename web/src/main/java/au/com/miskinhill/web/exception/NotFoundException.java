package au.com.miskinhill.web.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends WebApplicationException {

    private static final long serialVersionUID = 2737069398516387915L;
    
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }
    
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
