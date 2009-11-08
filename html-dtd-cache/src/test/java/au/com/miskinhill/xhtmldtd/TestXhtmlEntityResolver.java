package au.com.miskinhill.xhtmldtd;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;

public class TestXhtmlEntityResolver {

	@Test
	public void checkClasspath1() throws Exception {
		assertNotNull(XhtmlEntityResolver.class.getResourceAsStream("xhtml1-strict.dtd"));
	}
	@Test
	public void checkClasspath2() throws Exception {
		assertNotNull(XhtmlEntityResolver.class.getResourceAsStream("xhtml1-transitional.dtd"));
	}
	
    @Test
    public void testResolver() throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        documentBuilder.setEntityResolver(new XhtmlEntityResolver());
        documentBuilder.parse(getClass().getResource("sample.xhtml").toString());
    }
    
    @Test
    public void testResolverStrict() throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        documentBuilder.setEntityResolver(new XhtmlEntityResolver());
        documentBuilder.parse(getClass().getResource("sample-strict.xhtml").toString());
    }

    @Test
    public void testResolverMissingFile() throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        documentBuilder.setEntityResolver(new XhtmlEntityResolver());
        try {
            documentBuilder.parse(getClass().getResource("sample-missing.xhtml").toString());
            Assert.fail("Should have failed to find the DTD!");
        } catch(IllegalStateException ae) {
            assert ae.getMessage().startsWith("Entity should have been resolved from the cache");            
        }
    }
    
}
