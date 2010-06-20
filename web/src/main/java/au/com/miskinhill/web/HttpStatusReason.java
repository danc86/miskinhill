package au.com.miskinhill.web;

import org.springframework.http.HttpStatus;

public enum HttpStatusReason {
    
    // only the 400s because I am slack
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "Payment Required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(HttpStatus.PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "Request Timeout"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    GONE(HttpStatus.GONE, "Gone"),
    LENGTH_REQUIRED(HttpStatus.LENGTH_REQUIRED, "Length Required"),
    PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(HttpStatus.REQUEST_ENTITY_TOO_LARGE, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(HttpStatus.REQUEST_URI_TOO_LONG, "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(HttpStatus.EXPECTATION_FAILED, "Expectation Failed");
    
    private final HttpStatus status;
    private final String reason;
    
    private HttpStatusReason(HttpStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }
    
    public static String forStatusCode(int statusCode) {
        for (HttpStatusReason reason: values()) {
            if (reason.status.value() == statusCode) return reason.reason;
        }
        return "";
    }
    
    /** Convenience for JSP */
    public static String forStatusCode(Object statusCode) {
        if (statusCode instanceof String)
            return forStatusCode(new Integer((String) statusCode).intValue());
        else if (statusCode instanceof Integer)
            return forStatusCode(((Integer) statusCode).intValue());
        else return "";
    }

}
