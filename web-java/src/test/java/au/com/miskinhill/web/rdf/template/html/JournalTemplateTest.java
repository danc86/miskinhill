package au.com.miskinhill.web.rdf.template.html;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.junit.Test;

import au.com.miskinhill.rdftemplate.TemplateInterpolator;
import au.com.miskinhill.rdftemplate.datatype.DateDataType;

public class JournalTemplateTest {
    
    @BeforeClass
    public static void ensureDatatypesRegistered() {
        DateDataType.registerStaticInstance();
    }
    
    @Test
    public void shouldWork() throws Exception {
        Model model = loadModel();
        String result = render(model.getResource("http://miskinhill.com.au/journals/test/"));
        String expected = exhaust(this.getClass().getResource("Journal.out.xml").toURI());
        assertEquals(expected.trim(), result.trim());
    }
    
    private Model loadModel() {
        Model m = ModelFactory.createDefaultModel();
        InputStream stream = this.getClass().getResourceAsStream("/au/com/miskinhill/rdf/test.xml");
        if (stream == null) {
            throw new RuntimeException();
        }
        m.read(stream, "");
        return m;
    }
    
    private String render(RDFNode node) throws IOException, XMLStreamException {
        return TemplateInterpolator.interpolate(
                new InputStreamReader(this.getClass().getResourceAsStream("Journal.xml")),
                node);
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
