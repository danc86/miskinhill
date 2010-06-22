package au.com.miskinhill.web.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ResponseUtils {
    
    public static <T> ResponseEntity<T> createResponse(T body, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return new ResponseEntity<T>(body, headers, HttpStatus.OK);
    }
    
    private ResponseUtils() {
    }

}
