package au.com.miskinhill.rdftemplate;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.miskinhill.rdftemplate.datatype.DateDataType;

public class TemplateInterpolatorUnitTest {
    
    @BeforeClass
    public static void ensureDatatypesRegistered() {
        DateDataType.registerStaticInstance();
    }
    
    private Model model;
    
    @Before
    public void setUp() {
        model = ModelFactory.createDefaultModel();
        InputStream stream = this.getClass().getResourceAsStream(
                "/au/com/miskinhill/rdftemplate/test-data.xml");
        model.read(stream, "");
    }
    
    @Test
    public void shouldReplaceSubtreesWithContent() throws Exception {
        Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
        String result = TemplateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("replace-subtree.xml")), journal);
        assertThat(result, containsString("<div xml:lang=\"en\" lang=\"en\">A journal, you know, with some stuff in it</div>"));
        assertThat(result, not(containsString("<p>This should all go <em>away</em>!</p>")));
    }
    
    @Test
    public void shouldWork() throws Exception {
        Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
        String result = TemplateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("test-template.xml")), journal);
        String expected = exhaust(this.getClass().getResource("test-template.out.xml").toURI());
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
