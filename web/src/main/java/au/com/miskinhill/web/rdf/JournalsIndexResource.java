package au.com.miskinhill.web.rdf;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.miskinhill.web.TrailingSlash;
import au.com.miskinhill.web.TrailingSlashInterceptor;

@Controller
public class JournalsIndexResource {
    
    /**
     * @param request is needed for {@link TrailingSlashInterceptor}
     */
    @RequestMapping(value = {"/journals", "/journals/"}, method = RequestMethod.GET)
    @TrailingSlash
    public ModelAndView getJournalsIndex(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("journals-index");
        mav.addObject("nodeUri", "http://miskinhill.com.au/#organisation");
        return mav;
    }

}
