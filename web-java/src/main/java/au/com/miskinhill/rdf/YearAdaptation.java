package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.joda.time.LocalDate;

import au.com.miskinhill.rdftemplate.datatype.Year;
import au.com.miskinhill.rdftemplate.datatype.YearMonth;
import au.com.miskinhill.rdftemplate.selector.Adaptation;
import au.com.miskinhill.rdftemplate.selector.SelectorEvaluationException;

public class YearAdaptation implements Adaptation<Year> {

    @Override
    public Year adapt(RDFNode node) {
        if (!node.isLiteral())
            throw new SelectorEvaluationException("Attempted to apply #comparable-lv to non-literal node " + node);
        Object lv = ((Literal) node).getValue();
        if (lv instanceof LocalDate)
            return new Year((LocalDate) lv);
        else if (lv instanceof YearMonth)
            return new Year((YearMonth) lv);
        else if (lv instanceof Year)
            return (Year) lv;
        else
            throw new SelectorEvaluationException("Attempted to apply #year to non-date node " + node);
    }

    @Override
    public Class<Year> getDestinationType() {
        return Year.class;
    }

}
