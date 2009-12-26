package au.com.miskinhill;

import java.net.MalformedURLException;

import net.sourceforge.jwebunit.junit.WebTester;

public abstract class AbstractJWebUnitTest extends AbstractWebIntegrationTest {
    
    protected WebTester tester = new WebTester();
    
    public AbstractJWebUnitTest() {
        try {
            tester.setBaseUrl(BASE.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
