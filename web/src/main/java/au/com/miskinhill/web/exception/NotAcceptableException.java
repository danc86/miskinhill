package au.com.miskinhill.web.exception;

import org.springframework.http.HttpStatus;

public class NotAcceptableException extends WebApplicationException {
    
    private static final long serialVersionUID = 1302228521673280293L;

    public NotAcceptableException() {
        super(HttpStatus.NOT_ACCEPTABLE);
    }

}
