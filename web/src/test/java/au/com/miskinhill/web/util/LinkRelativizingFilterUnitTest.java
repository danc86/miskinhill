package au.com.miskinhill.web.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LinkRelativizingFilterUnitTest {
    
    private Filter filter;
    
    @Before
    public void setUp() throws Exception {
        filter = new LinkRelativizingFilter();
        filter.init(new MockFilterConfig());
    }
    
    @Test
    public void shouldRelativizeAnchors() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<body><a href=\"http://miskinhill.com.au/authors/mcnair-j\">John McNair</a></body>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString("<a href=\"/authors/mcnair-j\">John McNair</a>"));
    }
    
    @Test
    public void shouldRelativizeLinks() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<head><link rel=\"alternate\" href=\"http://miskinhill.com.au/authors/mcnair-j.xml\" /></head>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString("<link rel=\"alternate\" href=\"/authors/mcnair-j.xml\" />"));
    }
    
    @Test
    public void shouldRelativizeImages() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<body><img src=\"http://miskinhill.com.au/journals/test/1:1/cover.jpg\" /></body>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString("<img src=\"/journals/test/1:1/cover.jpg\" />"));
    }
    
    @Test
    public void shouldIgnoreAnchorWithoutHref() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<body><p><a name=\"lulz\" /></p></body>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString("<a name=\"lulz\" />"));
    }
    
    @Test
    public void shouldNotProduceBrokenScriptTags() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<head><script src=\"http://miskinhill.com.au/script/asdf.js\"></script></head>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString("<script src=\"/script/asdf.js\"></script>"));
    }
    
    @Test
    public void shouldDoMultipleReplacementsInOneDocument() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new TerminalFilterChain(
                html("<head><script src=\"http://miskinhill.com.au/script/asdf.js\"></script>" +
                		"<script src=\"http://miskinhill.com.au/script/xyz.js\"></script></head>")));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getCharacterEncoding(), equalTo("UTF-8"));
        assertThat(response.getContentAsString(), containsString(
                "<script src=\"/script/asdf.js\"></script><script src=\"/script/xyz.js\"></script>"));
    }
    
    private String html(String stuff) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
        		"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
        		stuff +
        		"</html>";
    }
    
    private static final class TerminalFilterChain implements FilterChain {
        
        private final String body;
        
        public TerminalFilterChain(String body) {
            this.body = body;
        }
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
            response.getWriter().append(body);
        }
        
    }

}
