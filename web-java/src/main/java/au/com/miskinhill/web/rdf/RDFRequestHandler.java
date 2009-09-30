package au.com.miskinhill.web.rdf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdftemplate.TemplateInterpolator;

@Component("rdfRequestHandler")
public class RDFRequestHandler implements HttpRequestHandler {
    
    private final Model model;
    
    @Autowired
    public RDFRequestHandler(Model model) {
        this.model = model;
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
        if (!resource.hasProperty(RDF.type, MHS.Journal)) {
            throw new ServletException("blargh implement me: " + Arrays.deepToString(resource.listProperties(RDF.type).toList().toArray()));
        }
        String body = TemplateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("template/html/Journal.xml")),
                resource);
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().append(body);
    }

}
