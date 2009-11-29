package au.com.miskinhill;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Ignore;

@Ignore
public final class TestUtil {
    
    ///CLOVER:OFF
    private TestUtil() {
    }
    ///CLOVER:ON
    
    public static String exhaust(InputStream stream) throws IOException { // sigh
        Reader reader = new InputStreamReader(stream);
        StringBuffer buff = new StringBuffer();
        int charsRead;
        char[] cb = new char[4096];
        while ((charsRead = reader.read(cb)) > 0) {
            buff.append(cb, 0, charsRead);
        }
        return buff.toString();
    }

}
