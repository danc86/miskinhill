package au.com.miskinhill.rdftemplate.selector;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FormattedDateTimeAdaptation implements Adaptation<String> {
    
    private final String pattern;
    private final DateTimeFormatter formatter;
    
    public FormattedDateTimeAdaptation(String pattern) {
        this.pattern = pattern;
        this.formatter = DateTimeFormat.forPattern(pattern.replace("\"", "'")); // for convenience in XML
    }

    public String getPattern() {
        return pattern;
    }
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }

    @Override
    public String adapt(RDFNode node) {
        if (!node.isLiteral()) {
            throw new SelectorEvaluationException("Attempted to apply #formatted-dt to non-literal node " + node);
        }
        Object lv = ((Literal) node).getValue();
        if (lv instanceof ReadableInstant) {
            ReadableInstant instant = (ReadableInstant) lv;
            return formatter.print(instant);
        } else if (lv instanceof ReadablePartial) {
            ReadablePartial instant = (ReadablePartial) lv;
            return formatter.print(instant);
        } else {
            throw new SelectorEvaluationException("Attempted to apply #formatted-dt to non-datetime literal " + lv);
        }
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pattern", pattern).toString();
    }

}
