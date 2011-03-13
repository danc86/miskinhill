package au.com.miskinhill.rdf;

import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Literal;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LCSHCleanupAdaptation extends AbstractAdaptation<String, Literal> {
    
    private final Pattern dashPattern = Pattern.compile("--(?!-)");
    
    public LCSHCleanupAdaptation() {
        super(String.class, new Class<?>[] { }, Literal.class);
    }
    
    @Override
    protected String doAdapt(Literal literal) {
        if (literal.getDatatype() != null)
            throw new SelectorEvaluationException("Attempted to apply #lcsh-cleanup to node with datatype " + literal.getDatatype());
        return dashPattern.matcher(literal.getValue().toString()).replaceAll(" \u2013 ");
    }

}
