package au.com.miskinhill.web.oaipmh;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.dom4j.Document;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import au.com.miskinhill.AbstractWebIntegrationTest;

public class OaipmhWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldServeTextXml() {
        ResponseEntity<Document> response = restTemplate.getForEntity(BASE.resolve("/oaipmh?verb=Identify"), Document.class);
        assertThat(response.getHeaders().getContentType(), equalTo(MediaType.TEXT_XML));
    }

}
