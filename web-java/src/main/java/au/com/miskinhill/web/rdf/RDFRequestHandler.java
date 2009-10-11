package au.com.miskinhill.web.rdf;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import au.com.miskinhill.rdf.Representation;
import au.com.miskinhill.rdf.RepresentationFactory;

@Component("rdfRequestHandler")
public class RDFRequestHandler implements HttpRequestHandler {
    
    private final Model model;
    private final RepresentationFactory representationFactory;
    
    @Autowired
    public RDFRequestHandler(Model model, RepresentationFactory representationFactory) {
        this.model = model;
        this.representationFactory = representationFactory;
    }
    
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getMethod().equals("GET")) {
            try {
                doGet(request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            throw new HttpRequestMethodNotSupportedException(request.getMethod());
        }
    }
    
    private void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String uri = "http://miskinhill.com.au" + req.getContextPath() + req.getServletPath() +
                (req.getPathInfo() != null ? req.getPathInfo() : "");
        Resource resource = model.createResource(uri);
        if (!resource.listProperties().hasNext()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Set<Representation> representations = representationFactory.getRepresentationsForResource(resource);
        if (representations.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // should be not acceptable, once we do proper negotiation
            return;
        }
        Representation representation = representations.iterator().next(); // XXX
        resp.setContentType(representation.getContentType().toString());
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().append(representation.render(resource));
    }

}
