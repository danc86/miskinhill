package au.com.miskinhill.web.rdf;

import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class RDFRequestHandlerIntegrationTest {
    
    @Autowired private RDFRequestHandler handler;
    
    @Test
    public void shouldAsdf() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setPathInfo("/journals/asees/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.handleRequest(request, response);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_OK));
//        assertThat(response.getContentAsString(), equalTo("asdf"));
    }

}
