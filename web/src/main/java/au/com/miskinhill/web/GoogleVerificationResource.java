package au.com.miskinhill.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

@Component
@Path("/google0c9dc78398234842.html")
public class GoogleVerificationResource {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "google-site-verification: google0c9dc78398234842.html"; 
    }

}
