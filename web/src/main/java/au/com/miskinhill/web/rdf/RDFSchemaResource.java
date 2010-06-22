package au.com.miskinhill.web.rdf;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.miskinhill.web.TrailingSlash;

@Controller
public class RDFSchemaResource {
    
    @RequestMapping(value = {"/rdfschema/1.0", "/rdfschema/1.0/"}, method = RequestMethod.GET)
    @TrailingSlash
    public ModelAndView getSchemaIndex(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("rdfschema-index");
        mav.addObject("nodeUri", "http://miskinhill.com.au/rdfschema/1.0/");
        return mav;
    }

}
