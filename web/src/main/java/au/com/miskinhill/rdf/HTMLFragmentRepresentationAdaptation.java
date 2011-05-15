package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;
import au.id.djc.rdftemplate.XMLStream;
import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HTMLFragmentRepresentationAdaptation extends AbstractAdaptation<XMLStream, RDFNode> {
    
    private static final Map<Resource, String> TYPE_TEMPLATES = new HashMap<Resource, String>();
    static {
        TYPE_TEMPLATES.put(MHS.Book, "template/htmlfragment/Book.xml");
        TYPE_TEMPLATES.put(MHS.Article, "template/htmlfragment/Article.xml");
    }
    
    private final TemplateInterpolator templateInterpolator;
    
    @Autowired
    public HTMLFragmentRepresentationAdaptation(TemplateInterpolator templateInterpolator) {
        super(XMLStream.class, new Class<?>[] { }, RDFNode.class);
        this.templateInterpolator = templateInterpolator;
    }
    
    @Override
    protected XMLStream doAdapt(RDFNode node) {
        String templatePath = null;
        for (Resource type: RDFUtil.getTypes(node.as(Resource.class))) {
            templatePath = TYPE_TEMPLATES.get(type);
            if (templatePath != null) break;
        }
        if (templatePath == null)
            throw new SelectorEvaluationException("No HTML fragment template found for node " + node);
        
        List<XMLEvent> destination = new ArrayList<XMLEvent>();
        templateInterpolator.interpolate(
                this.getClass().getResourceAsStream(templatePath),
                node, destination);
        if (destination.get(0).isStartDocument() && destination.get(destination.size() - 1).isEndDocument()) {
            destination.remove(destination.size() - 1);
            destination.remove(0);
        }
        return new XMLStream(destination);
    }

}
