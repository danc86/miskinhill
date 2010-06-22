package au.com.miskinhill.web.exception;

import org.springframework.http.HttpStatus;

public class WebApplicationException extends RuntimeException {
    
    private static final long serialVersionUID = -2893596192582537805L;
    
    private final HttpStatus status;
    
    public WebApplicationException(HttpStatus status) {
        this.status = status;
    }
    
    public WebApplicationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
}