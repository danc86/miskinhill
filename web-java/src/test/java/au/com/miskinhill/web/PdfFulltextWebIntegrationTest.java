package au.com.miskinhill.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class PdfFulltextWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldServePdfs() {
        ClientResponse response = Client.create().resource(BASE)
                .path("/journals/asees/22:1-2/location-in-russian-aluminium.pdf").get(ClientResponse.class);
        assertThat(response.getType(), equalTo(new MediaType("application", "pdf")));
        assertThat(response.getLength(), equalTo(213303));
        byte[] responseBody = response.getEntity(byte[].class);
        assertThat(responseBody.length, equalTo(213303));
        assertThat(new String(responseBody, 0, 8), equalTo("%PDF-1.4"));
    }
    
    @Test
    public void shouldSupportAcceptRange() {
        ClientResponse response = Client.create().resource(BASE)
                .path("/journals/asees/22:1-2/location-in-russian-aluminium.pdf")
                .header("Range", "bytes=500-999").get(ClientResponse.class);
        assertThat(response.getLength(), equalTo(500));
        assertThat(response.getHeaders().getFirst("Content-Range"), equalTo("bytes 500-999/213303"));
    }

}
