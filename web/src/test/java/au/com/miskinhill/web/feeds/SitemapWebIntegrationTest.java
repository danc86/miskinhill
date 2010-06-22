package au.com.miskinhill.web.feeds;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import au.com.miskinhill.AbstractWebIntegrationTest;
import au.com.miskinhill.schema.sitemaps.Url;
import au.com.miskinhill.schema.sitemaps.Urlset;

public class SitemapWebIntegrationTest extends AbstractWebIntegrationTest {
    
    @Test
    public void shouldServeAsTextXml() {
        ResponseEntity<Urlset> response= restTemplate.getForEntity(BASE.resolve("/feeds/sitemap"), Urlset.class);
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_XML));
    }
    
    @Test
    public void shouldContainAlternateRepresentations() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        assertTrue(sitemap.containsLoc("http://miskinhill.com.au/journals/asees/.mods"));
    }
    
    @Test
    public void shouldContainPdfFullTextUrls() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        assertTrue(sitemap.containsLoc("http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik.pdf"));
    }
    
    @Test
    public void shouldNotContainPdfFullTextUrlsForCitedJournals() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        assertFalse(sitemap.containsLoc("http://miskinhill.com.au/cited/journals/ajph/45:1/all-union-society.pdf"));
    }
    
    @Test
    public void shouldNotContainDuplicates() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        Set<String> locs = new HashSet<String>();
        for (Url url: sitemap.getUrls()) {
            assertFalse("Found duplicate " + url, locs.contains(url.getLoc()));
            locs.add(url.getLoc());
        }
    }
    
    @Test
    public void shouldContainDataDumpLocation() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        assertThat(sitemap.getDatasets().size(), equalTo(1));
        assertThat(sitemap.getDatasets().get(0).getDataDumpLocation(), equalTo("http://miskinhill.com.au/feeds/world"));
    }

}
