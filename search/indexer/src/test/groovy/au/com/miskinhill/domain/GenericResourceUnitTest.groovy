package au.com.miskinhill.domain

import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.equalTo

import org.junit.Test
import com.hp.hpl.jena.datatypes.RDFDatatypeimport com.hp.hpl.jena.datatypes.TypeMapper
import com.hp.hpl.jena.rdf.model.Literalimport com.hp.hpl.jena.rdf.model.ResourceFactoryclass GenericResourceUnitTest {
    
    @Test
    void toHTMLShouldEscapeUntypedLiterals() {
        def literal = ResourceFactory.createPlainLiteral("1 < 2, & you know it")
        assertThat GenericResource.toHTML(literal), equalTo("1 &lt; 2, &amp; you know it")
    }
    
    @Test
    void toHTMLShouldNotEscapeXMLLiterals() {
        def xml = '''<span xmlns="http://www.w3.org/1999/xhtml" lang="en"><em>Nabat</em> and its editors:
                the 1919 swansong of the Brisbane Russian socialist press, or
                <em lang="ru">Нас еще судьбы безвестные ждут</em></span>'''
        def literal = ResourceFactory.createTypedLiteral(xml,
                TypeMapper.getInstance().getTypeByName("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"))
        assertThat GenericResource.toHTML(literal), equalTo(xml)
    }
    
    @Test(expected=IllegalArgumentException)
    void toHTMLShouldThrowForUnknownLiteralType() {
        def literal = ResourceFactory.createTypedLiteral('asdf',
                TypeMapper.getInstance().getSafeTypeByName("http://example.com/custom-type"))
        GenericResource.toHTML(literal)
    }
    
}