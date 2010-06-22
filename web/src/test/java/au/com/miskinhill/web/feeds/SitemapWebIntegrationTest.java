package au.com.miskinhill.web.feeds;

import static au.com.miskinhill.MiskinHillMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
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
        Url url = sitemap.getUrlForLoc("http://miskinhill.com.au/journals/asees/.mods");
        assertThat(url, not(nullValue()));
    }
    
    @Test
    public void shouldContainPdfFullTextUrls() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        Url url = sitemap.getUrlForLoc("http://miskinhill.com.au/journals/asees/22:1-2/post-soviet-boevik.pdf");
        assertThat(url, not(nullValue()));
    }
    
    @Test
    public void shouldNotContainPdfFullTextUrlsForCitedJournals() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        Url url = sitemap.getUrlForLoc("http://miskinhill.com.au/cited/journals/ajph/45:1/all-union-society.pdf");
        assertThat(url, nullValue());
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
    
    @SuppressWarnings("unchecked") // joda
    @Test
    public void shouldContainLastModifiedTimestamps() {
        Urlset sitemap = restTemplate.getForObject(BASE.resolve("/feeds/sitemap"), Urlset.class);
        Url url = sitemap.getUrlForLoc("http://miskinhill.com.au/journals/asees/");
        assertThat(url.getLastmod(), greaterThan(new DateTime("2010-06-22T21:00:00+10:00")));
    }
    
    @SuppressWarnings("unchecked") // joda
    @Test
    public void httpLastModifiedShouldEqualLatestLastmod() {
        ResponseEntity<Urlset> response = restTemplate.getForEntity(BASE.resolve("/feeds/sitemap"), Urlset.class);
        List<DateTime> lastmods = new ArrayList<DateTime>();
        for (Url url: response.getBody().getUrls()) {
            if (url.getLastmod() != null)
                lastmods.add(url.getLastmod());
        }
        assertTrue(new DateTime(response.getHeaders().getLastModified()).isEqual(Collections.max(lastmods)));
    }
    
    @Test
    public void shouldSupportIfModifiedSince() {
        ResponseEntity<Urlset> response = restTemplate.getForEntity(BASE.resolve("/feeds/sitemap"), Urlset.class);
        DateTime lastModified = new DateTime(response.getHeaders().getLastModified());
        assertNotModifiedSince(BASE.resolve("/feeds/sitemap"), lastModified.plusMinutes(1));
    }

}
