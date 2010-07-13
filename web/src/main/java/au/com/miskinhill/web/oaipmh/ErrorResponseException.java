package au.com.miskinhill.web.oaipmh;

import au.com.miskinhill.schema.oaipmh.Error;
import au.com.miskinhill.schema.oaipmh.ErrorCode;

public class ErrorResponseException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private final Error error;
    
    public ErrorResponseException(ErrorCode errorCode, String message) {
        super("OAI-PMH protocol error: " + errorCode.name() + ": " + message);
        this.error = new Error(errorCode, message);
    }
    
    public Error getError() {
        return error;
    }

}