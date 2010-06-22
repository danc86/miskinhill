package au.com.miskinhill.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GoogleVerificationController {
    
    @RequestMapping(value = "/google0c9dc78398234842.html", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> get() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>("google-site-verification: google0c9dc78398234842.html",
                headers, HttpStatus.OK);
    }

}
