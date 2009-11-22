package au.com.miskinhill;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.junit.Ignore;

@Ignore
public final class TestUtil {
    
    ///CLOVER:OFF
    private TestUtil() {
    }
    ///CLOVER:ON
    
    public static String exhaust(URI file) throws IOException { // sigh
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
