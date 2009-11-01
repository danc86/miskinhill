package au.com.miskinhill.rdf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.junit.BeforeClass;

import com.hp.hpl.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
public class HTMLRepresentationTest {
    
    @BeforeClass
    public static void deleteme() {
        System.setProperty("contentPath", "/home/dan/miskinhill/content");
    }

    @Autowired private RepresentationFactory representationFactory;
    private Representation representation;
    private Model model;
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.load(HTMLRepresentationTest.class, "/au/com/miskinhill/rdf/test.xml");
        representation = representationFactory.getRepresentationByFormat("html");
    }
    
    @Test
    public void testJournal() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/journals/test/"));
        String expected = exhaust(this.getClass().getResource("template/html/Journal.out.xml").toURI());
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testAuthor() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/authors/test-author"));
        String expected = exhaust(this.getClass().getResource("template/html/Author.out.xml").toURI());
        assertEquals(expected.trim(), result.trim());
    }
    
    @Test
    public void testForum() throws Exception {
        String result = representation.render(model.getResource("http://miskinhill.com.au/"));
        String expected = exhaust(this.getClass().getResource("template/html/Forum.out.xml").toURI());
        assertEquals(expected.trim(), result.trim());
    }
    
    private String exhaust(URI file) throws IOException { // sigh
        FileChannel channel = new FileInputStream(new File(file)).getChannel();
        Charset charset = Charset.defaultCharset();
        StringBuffer sb = new StringBuffer();
        ByteBuffer b = ByteBuffer.allocate(8192);
        while (channel.read(b) > 0) {
            b.rewind();
            sb.append(charset.decode(b));
            b.flip();
        }
        return sb.toString();
    }

}
