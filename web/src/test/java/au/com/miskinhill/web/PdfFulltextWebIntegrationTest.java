package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class PdfFulltextWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldServePdfs() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                BASE.resolve("/journals/asees/22:1-2/location-in-russian-aluminium.pdf"), byte[].class);
        assertThat(response.getHeaders().getContentType(), equalTo(new MediaType("application", "pdf")));
        assertThat(response.getHeaders().getContentLength(), equalTo(213303L));
        assertThat(response.getBody().length, equalTo(213303));
        assertThat(new String(response.getBody(), 0, 8), equalTo("%PDF-1.4"));
    }
    
    @Test
    public void shouldSupportAcceptRange() {
       restTemplate.execute(
                BASE.resolve("/journals/asees/22:1-2/location-in-russian-aluminium.pdf"),
                HttpMethod.GET, new RequestCallback() {
                    @Override
                    public void doWithRequest(ClientHttpRequest request) throws IOException {
                        request.getHeaders().set("Range", "bytes=500-999");
                    }
                }, new ResponseExtractor<Object>() {
                    @Override
                    public Object extractData(ClientHttpResponse response) throws IOException {
                        assertThat(response.getHeaders().getContentLength(), equalTo(500L));
                        assertThat(response.getHeaders().getFirst("Content-Range"), equalTo("bytes 500-999/213303"));
                        return null;
                    }
                });
    }

}
