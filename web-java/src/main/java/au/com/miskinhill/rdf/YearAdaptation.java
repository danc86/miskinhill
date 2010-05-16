package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.datatype.Year;
import au.id.djc.rdftemplate.datatype.YearMonth;
import au.id.djc.rdftemplate.selector.AbstractAdaptation;
import au.id.djc.rdftemplate.selector.SelectorEvaluationException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class YearAdaptation extends AbstractAdaptation<Year, Literal> {

    public YearAdaptation() {
        super(Year.class, new Class<?>[] { }, Literal.class);
    }

    @Override
    protected Year doAdapt(Literal node) {
        Object lv = node.getValue();
        if (lv instanceof LocalDate)
            return new Year((LocalDate) lv);
        else if (lv instanceof YearMonth)
            return new Year((YearMonth) lv);
        else if (lv instanceof Year)
            return (Year) lv;
        else
            throw new SelectorEvaluationException("Attempted to apply #year to non-date node " + node);
    }

}
