package au.com.miskinhill.web.feeds;

import static org.junit.Assert.*;

import com.sun.jersey.api.client.Client;
import org.junit.Test;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.schema.sitemaps.Urlset;

public class SitemapWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldBeParseable() {
        Urlset sitemap = Client.create().resource(BASE).path("/feeds/sitemap").get(Urlset.class);
        assertTrue(sitemap.getUrls().size() >= 1228);
    }

}
