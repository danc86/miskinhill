package au.com.miskinhill;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpMethodBase;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;


public abstract class AbstractWebIntegrationTest {
    
    protected static final URI BASE = URI.create("http://localhost:8082/");
    protected static final RestTemplate restTemplate = new RestTemplate(new CommonsClientHttpRequestFactory());
    static {
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        restTemplate.getMessageConverters().add(new Jaxb2RootElementHttpMessageConverter());
        restTemplate.getMessageConverters().add(new Dom4jDocumentMessageConverter());
    }
    
    private static final RestTemplate nonFollowingRestTemplate = new RestTemplate(new CommonsClientHttpRequestFactory() {
        @Override
        protected void postProcessCommonsHttpMethod(HttpMethodBase httpMethod) {
            httpMethod.setFollowRedirects(false);
        }
    });
    private static final RestTemplate nonThrowingRestTemplate = new RestTemplate(new CommonsClientHttpRequestFactory());
    static {
        nonThrowingRestTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                throw new UnsupportedOperationException();
            }
        });
    }
    
    protected void assertHttpError(URI url, final HttpStatus errorStatus) {
        nonThrowingRestTemplate.execute(url, HttpMethod.GET, null, new ResponseExtractor<Object>() {
            @Override
            public Object extractData(ClientHttpResponse response) throws IOException {
                assertThat(response.getStatusCode(), equalTo(errorStatus));
                return null;
            }
        });
    }
    
    protected void assertRedirect(URI from, final URI to) {
        assertRedirect(from, to, HttpStatus.FOUND);
    }
    
    protected void assertRedirect(URI from, final URI to, final HttpStatus redirectStatus) {
        nonFollowingRestTemplate.execute(from, HttpMethod.GET, null, new ResponseExtractor<Object>() {
            @Override
            public Object extractData(ClientHttpResponse response) throws IOException {
                assertThat(response.getStatusCode(), equalTo(redirectStatus));
                assertThat(response.getHeaders().getLocation(), equalTo(to));
                return null;
            }
        });
    }

}
