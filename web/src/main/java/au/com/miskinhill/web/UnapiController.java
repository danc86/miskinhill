package au.com.miskinhill.web;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.miskinhill.rdf.Representation;
import au.com.miskinhill.rdf.Representation.ShownIn;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.schema.unapi.Format;
import au.com.miskinhill.schema.unapi.Formats;
import au.com.miskinhill.web.exception.NotAcceptableException;
import au.com.miskinhill.web.exception.NotFoundException;
import au.com.miskinhill.web.exception.RedirectException;

@Controller
public class UnapiController {
    
    private final Model model;
    private final RepresentationFactory representationFactory;
    private final Formats allFormats; // computed once for efficiency
    
    @Autowired
    public UnapiController(Model model, RepresentationFactory representationFactory) {
        this.model = model;
        this.representationFactory = representationFactory;
        
        allFormats = new Formats();
        for (Representation r: representationFactory.getAll())
            allFormats.add(new Format(r));
    }
    
    @RequestMapping(value = "/unapi", method = RequestMethod.GET)
    @ResponseBody
    public Formats unapi(@RequestParam(required = false) String id, @RequestParam(required = false) String format) {
        if (id != null) {
            Resource resource = model.createResource(id);
            if (!resource.listProperties().hasNext())
                throw new NotFoundException();
            if (format != null) {
                Representation representation = representationFactory.getRepresentationByFormat(format);
                if (representation == null)
                    throw new NotAcceptableException();
                if (!representation.canRepresent(resource))
                    throw new NotAcceptableException();
                throw new RedirectException(id + "." + format);
            }
            List<Representation> representations = representationFactory.getRepresentationsForResource(resource);
            CollectionUtils.filter(representations, SHOWN_IN_UNAPI_PREDICATE);
            return Formats.forId(id, representations);
        }
        return allFormats;
    }
    
    private static final Predicate<Representation> SHOWN_IN_UNAPI_PREDICATE = new Predicate<Representation>() {
        @Override
        public boolean evaluate(Representation representation) {
            return representation.isShownIn(ShownIn.Unapi);
        }
    };

}
