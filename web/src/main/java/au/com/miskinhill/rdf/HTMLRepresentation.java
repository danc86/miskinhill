package au.com.miskinhill.rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdf.vocabulary.SIOC;

@Component
public class HTMLRepresentation implements Representation {
    
    private static final String CITED_PREFIX = "http://miskinhill.com.au/cited/";

    private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.AtomLinks, ShownIn.Unapi);
    private final Map<Resource, String> typeTemplates = new HashMap<Resource, String>();
    private final Map<Resource, String> citedTypeTemplates = new HashMap<Resource, String>();
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public HTMLRepresentation(TemplateInterpolator templateInterpolator) {
        this.templateInterpolator = templateInterpolator;
        
        typeTemplates.put(SIOC.Forum, "template/html/Forum.xml");
        typeTemplates.put(MHS.Author, "template/html/Author.xml");
        typeTemplates.put(MHS.Article, "template/html/Article.xml");
        typeTemplates.put(MHS.Obituary, "template/html/Obituary.xml");
        typeTemplates.put(MHS.Review, "template/html/Review.xml");
        typeTemplates.put(MHS.Issue, "template/html/Issue.xml");
        typeTemplates.put(MHS.Journal, "template/html/Journal.xml");
        typeTemplates.put(RDFS.Class, "template/html/Class.xml");
        typeTemplates.put(RDF.Property, "template/html/Property.xml");
        
        citedTypeTemplates.put(MHS.Article, "template/html/CitedArticle.xml");
        citedTypeTemplates.put(MHS.Book, "template/html/Book.xml");
    }

    @Override
    public boolean canRepresent(Resource resource) {
        if (resource.getURI().startsWith("http://miskinhill.com.au/cited/"))
            return RDFUtil.hasAnyType(resource, citedTypeTemplates.keySet());
        return RDFUtil.hasAnyType(resource, typeTemplates.keySet());
    }

    @Override
    public MediaType getContentType() {
        return MediaType.TEXT_HTML;
    }
    
    @Override
    public Collection<MediaType> getContentTypeAliases() {
        return Collections.singleton(MediaType.APPLICATION_XHTML_XML);
    }
    
    @Override
    public String getFormat() {
        return "html";
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public String getLabel() {
        return "HTML";
    }
    
    @Override
    public String getDocs() {
        return "http://www.w3.org/TR/xhtml1/";
    }
    
    @Override
    public boolean isShownIn(ShownIn place) {
        return shownIn.contains(place);
    }

    @Override
    public String render(Resource resource) {
        for (Resource type: RDFUtil.getTypes(resource)) {
            String templatePath;
            if (resource.getURI().startsWith(CITED_PREFIX))
                templatePath = citedTypeTemplates.get(type);
            else
                templatePath = typeTemplates.get(type);
            if (templatePath != null) {
                return templateInterpolator.interpolate(
                        this.getClass().getResourceAsStream(templatePath),
                        resource);
            }
        }
        throw new IllegalArgumentException("No template found for " + resource);
    }

}
