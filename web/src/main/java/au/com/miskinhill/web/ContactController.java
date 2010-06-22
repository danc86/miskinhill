package au.com.miskinhill.web;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ContactController {

    private static final String NOTIFICATION_COOKIE_NAME = "miskinhill-notification";
    private static final String SUBMITTED_MESSAGE = "Your%20feedback%20has%20been%20submitted.";
    private static final String SUBMIT_REDIRECT = "/contact/";
    
    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;
    
    // for stupid cglib
    public ContactController() {
        this.mailSender = null;
        this.templateMessage = null;
    }
    
    @Autowired
    public ContactController(MailSender mailSender, @Qualifier("feedbackSubmissionTemplate") SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
    }
    
    @RequestMapping(value = {"/contact", "/contact/"}, method = RequestMethod.GET)
    @TrailingSlash
    public ModelAndView getForm(HttpServletRequest request,
            @RequestHeader(value = "Referer", required = false) String referer,
            @CookieValue(value = NOTIFICATION_COOKIE_NAME, required = false) String notificationCookie) throws URISyntaxException {
        String prefilledBody = "";
        if (referer != null && notificationCookie == null) {
            prefilledBody = String.format("Regarding the page at <%s>, ", referer);
        }
        ModelAndView mav = new ModelAndView("contact");
        mav.addObject("body", prefilledBody);
        return mav;
    }
    
    @RequestMapping(value = "/contact/submit", method = RequestMethod.POST)
    public void submit(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("body") String body, @RequestParam("from") String from,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) throws IOException {
        SimpleMailMessage message = new SimpleMailMessage(templateMessage);
        message.setText(String.format("%s\n\nFrom: %s\nUser-Agent: %s", body, from, userAgent));
        mailSender.send(message);
        
        Cookie cookie = new Cookie(NOTIFICATION_COOKIE_NAME, SUBMITTED_MESSAGE);
        cookie.setDomain(request.getServerName());
        cookie.setMaxAge(20);
        cookie.setSecure(request.getScheme().equals("https"));
        response.addCookie(cookie);
        
        response.setStatus(HttpStatus.SEE_OTHER.value());
        response.sendRedirect(SUBMIT_REDIRECT);
    }

}
