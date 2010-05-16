package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import au.com.miskinhill.AbstractJWebUnitTest;

public class ContactWebIntegrationTest extends AbstractJWebUnitTest {
    
    @Test
    public void shouldRedirectForMissingTrailingSlash() throws Exception {
        tester.beginAt("/contact");
        assertThat(tester.getTestingEngine().getPageURL().getPath(), equalTo("/contact/"));
    }
    
    @Test
    public void shouldHaveCorrectTitle() throws Exception {
        tester.beginAt("/contact/");
        tester.assertTitleEquals("Contact - Miskin Hill");
    }
    
    @Test
    public void shouldBeDecorated() throws Exception {
        tester.beginAt("/contact/");
        tester.getElementByXPath("//head/link[@rel='stylesheet' and @type='text/css' and @href='/style/common.css']");
    }
    
    @Test
    public void shouldPrefillFeedbackWithReferringUrl() throws Exception {
        tester.beginAt("/");
        tester.clickLinkWithExactText("Feedback");
        tester.assertTextFieldEquals("body", String.format("Regarding the page at <%s>, ", BASE));
    }

    @Test
    public void shouldNotPrefillWhenNoReferrer() throws Exception {
        tester.beginAt("/contact/");
        tester.assertTextFieldEquals("body", "");
    }
    
    @Test
    public void shouldAcceptSubmissions() throws Exception {
        tester.beginAt("/contact/");
        tester.setTextField("body", "integration test feedback body");
        tester.setTextField("from", "integration-test@miskinhill.com.au");
        tester.submit();
        tester.assertTitleEquals("Contact - Miskin Hill");
        tester.assertTextPresent("Your feedback has been submitted.");
        tester.assertTextFieldEquals("body", "");
    }
    
}
