package au.com.miskinhill.domain;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Literal;import com.hp.hpl.jena.rdf.model.ResourceFactory;public class GenericResourceUnitTest {
    
    @Test
    public void toHTMLShouldEscapeUntypedLiterals() {
        Literal literal = ResourceFactory.createPlainLiteral("1 < 2, & you know it");
        assertThat(GenericResource.toHTML(literal), equalTo("1 &lt; 2, &amp; you know it"));
    }
    
    @Test
    public void toHTMLShouldNotEscapeXMLLiterals() {
        String xml = "<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><em>Nabat</em> and its editors: " +
                "the 1919 swansong of the Brisbane Russian socialist press, or" +
                "<em lang=\"ru\">Нас еще судьбы безвестные ждут</em></span>";
        Literal literal = ResourceFactory.createTypedLiteral(xml,
                TypeMapper.getInstance().getTypeByName("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"));
        assertThat(GenericResource.toHTML(literal), equalTo(xml));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void toHTMLShouldThrowForUnknownLiteralType() {
        Literal literal = ResourceFactory.createTypedLiteral("asdf",
                TypeMapper.getInstance().getSafeTypeByName("http://example.com/custom-type"));
        GenericResource.toHTML(literal);
    }
    
}