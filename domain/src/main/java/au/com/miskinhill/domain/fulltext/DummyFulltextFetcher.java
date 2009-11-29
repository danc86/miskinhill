package au.com.miskinhill.domain.fulltext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Dummy implementation of {@link FulltextFetcher} which always returns an
 * {@link InputStream} containing its fulltext property. For use in tests.
 */
public class DummyFulltextFetcher implements FulltextFetcher {
    
    private String fulltext;
    
    public String getFulltext() {
        return fulltext;
    }
    
    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }
    
    @Override
    public InputStream fetchFulltext(String path) throws IOException {
        return new ByteArrayInputStream(fulltext.getBytes("UTF-8"));
    }

}
