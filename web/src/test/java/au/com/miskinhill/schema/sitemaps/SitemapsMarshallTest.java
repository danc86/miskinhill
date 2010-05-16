package au.com.miskinhill.schema.sitemaps;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.math.BigDecimal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class SitemapsMarshallTest {

    private static JAXBContext jc;
    static {
        try {
            jc = JAXBContext.newInstance("au.com.miskinhill.schema.sitemaps");
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldMarshall() throws Exception {
        Urlset urlset = new Urlset();
        urlset.add(new Url("http://example.com/asdf"));
        urlset.add(new Url("http://example.com/another", "2009-09-14", ChangeFreq.MONTHLY, BigDecimal.ONE));

        Marshaller m = jc.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new SitemapsNamespacePrefixMapper());
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter w = new StringWriter();
        m.marshal(urlset, w);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:sc=\"http://sw.deri.org/2007/07/sitemapextension/scschema.xsd\">\n" + 
        		"    <url>\n" + 
        		"        <loc>http://example.com/asdf</loc>\n" + 
        		"    </url>\n" + 
        		"    <url>\n" + 
        		"        <loc>http://example.com/another</loc>\n" + 
        		"        <lastmod>2009-09-14</lastmod>\n" + 
        		"        <changefreq>MONTHLY</changefreq>\n" + 
        		"        <priority>1</priority>\n" + 
        		"    </url>\n" + 
        		"</urlset>\n",
        		w.toString());
    }
    
    @Test
    public void shouldMarshallWithDataset() throws Exception {
        Urlset urlset = new Urlset();
        urlset.add(new Url("http://example.com/asdf"));
        urlset.add(new Dataset("http://example.com/feeds/world"));
        
        Marshaller m = jc.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new SitemapsNamespacePrefixMapper());
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter w = new StringWriter();
        m.marshal(urlset, w);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:sc=\"http://sw.deri.org/2007/07/sitemapextension/scschema.xsd\">\n" + 
        		"    <url>\n" + 
        		"        <loc>http://example.com/asdf</loc>\n" + 
        		"    </url>\n" + 
        		"    <sc:dataset>\n" + 
        		"        <sc:dataDumpLocation>http://example.com/feeds/world</sc:dataDumpLocation>\n" + 
        		"    </sc:dataset>\n" + 
        		"</urlset>\n",
                w.toString());
    }

}
