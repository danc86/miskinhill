package au.com.miskinhill.web;

import java.nio.charset.Charset;
import java.util.BitSet;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.net.URLCodec;

/** Ugh */
public final class ProperURLCodec extends URLCodec {
    
    private static final BitSet SAFE = (BitSet) WWW_FORM_URL.clone();
    static { SAFE.clear(' '); }
    private static final Charset UTF8 = Charset.forName(CharEncoding.UTF_8);
    private static final Charset ASCII = Charset.forName(CharEncoding.US_ASCII);
    
    public static String encodeUrl(String raw) {
        return new String(encodeUrl(SAFE, raw.getBytes(UTF8)), ASCII);
    }
    
    private ProperURLCodec() {
    }
    
}