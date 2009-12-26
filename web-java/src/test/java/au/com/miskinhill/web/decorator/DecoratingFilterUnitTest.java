package au.com.miskinhill.web.decorator;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.matchers.JUnitMatchers.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.TestUtil;

public class DecoratingFilterUnitTest {
    
    private DecoratingFilter filter;
    
    @Before
    public void setUp() {
        filter = new DecoratingFilter();
    }
    
    @Test
    public void shouldBeginWithXmlDeclAndDtd() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertTrue(result.toString().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"));
    }
    
    @Test
    public void shouldReplaceTitle() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), containsString("<title>Some title - Miskin Hill</title>"));
    }
    
    @Test
    public void shouldCopyHeadElementsExceptTitle() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), not(containsString("<title>Some title</title>")));
        assertThat(result.toString(), containsString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/style.css\"/></head>"));
    }
    
    @Test
    public void shouldCopyBodyElements() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), containsString("<div id=\"stuff\"><p>Body stuff</p></div><div id=\"footer\">"));
    }
    
    @Test
    public void shouldCopyBodyAttributes() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), containsString("<body class=\"body-class\" base=\"asdf\">"));
    }
    
    @Test
    public void shouldCopyHeadAttributes() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), containsString("<head profile=\"http://microformats.org/wiki/hreview-profile\">"));
    }
    
    @Test
    public void shouldFillInIncompletenessWarning() throws Exception {
        String dummy = TestUtil.exhaust(this.getClass().getResourceAsStream("dummy-incomplete.xml"));
        CharSequence result = filter.postprocessResponse(dummy);
        assertThat(result.toString(), containsString("<div class=\"incompleteness-warning\" lang=\"en\">" + 
        		"<p>The full HTML version of this article is not currently available online. Please refer to the original print version.</p>" + 
        		"</div>"));
    }

}
