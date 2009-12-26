package au.com.miskinhill.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component
@Path("/contact/")
public class ContactResource {

    private final URI SUBMIT_REDIRECT = URI.create("/contact/");
    private final String SUBMITTED_MESSAGE = "Your%20feedback%20has%20been%20submitted.";
    private final String template;
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;
    
    @Autowired
    public ContactResource(MailSender mailSender, @Qualifier("feedbackSubmissionTemplate") SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
        try {
            this.template = FileCopyUtils.copyToString(new InputStreamReader(
                    this.getClass().getResourceAsStream("contact-template.html")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getForm(@Context UriInfo uriInfo, @HeaderParam("Referer") String referer,
            @CookieParam("miskinhill-notification") String notificationCookie) {
        String prefilledBody = "";
        if (referer != null && !uriInfo.getBaseUri().relativize(URI.create(referer)).isAbsolute() && notificationCookie == null) {
            prefilledBody = String.format("Regarding the page at &lt;%s&gt;, ", referer);
        }
        return String.format(template, prefilledBody);
    }
    
    @POST
    @Path("submit")
    public Response submit(@Context UriInfo uriInfo, @FormParam("body") String body, @FormParam("from") String from,
            @HeaderParam("User-Agent") String userAgent) {
        SimpleMailMessage message = new SimpleMailMessage(templateMessage);
        message.setText(String.format("%s\n\nFrom: %s\nUser-Agent: %s", body, from, userAgent));
        mailSender.send(message);
        NewCookie notificationCookie = new NewCookie("miskinhill-notification", SUBMITTED_MESSAGE,
                null, uriInfo.getBaseUri().getHost(), "", 20, uriInfo.getBaseUri().getScheme().equals("https"));
        return Response.seeOther(SUBMIT_REDIRECT).cookie(notificationCookie).build();
    }

}
