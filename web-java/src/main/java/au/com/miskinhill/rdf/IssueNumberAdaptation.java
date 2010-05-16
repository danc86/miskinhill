package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import au.id.djc.rdftemplate.selector.AbstractAdaptation;

import au.com.miskinhill.rdf.vocabulary.MHS;

public class IssueNumberAdaptation extends AbstractAdaptation<String, Resource> {

    public IssueNumberAdaptation() {
        super(String.class, new Class<?>[] { }, Resource.class);
    }

    @Override
    protected String doAdapt(Resource node) {
        String issueNumber = node.getRequiredProperty(MHS.issueNumber).getObject().as(Literal.class).getValue().toString();
        String label = "No.\u00a0";
        if (issueNumber.indexOf('\u2012') >= 0 || issueNumber.indexOf('\u2013') >= 0) {
            label = "Nos.\u00a0";
        }
        return label + issueNumber;
    }

}
