package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.rdftemplate.selector.Adaptation;

public class IssueNumberAdaptation implements Adaptation<String> {
    
    @Override
    public Class<String> getDestinationType() {
        return String.class;
    }
    
    @Override
    public String adapt(RDFNode node) {
        Resource resource = node.as(Resource.class);
        String issueNumber = resource.getRequiredProperty(MHS.issueNumber).getObject()
                .as(Literal.class).getValue().toString();
        String label = "No.\u00a0";
        if (issueNumber.indexOf('\u2012') >= 0 || issueNumber.indexOf('\u2013') >= 0) {
            label = "Nos.\u00a0";
        }
        return label + issueNumber;
    }

}
