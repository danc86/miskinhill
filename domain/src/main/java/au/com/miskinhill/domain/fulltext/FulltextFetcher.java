package au.com.miskinhill.domain.fulltext;

import java.io.IOException;
import java.io.InputStream;

public interface FulltextFetcher {
    
    InputStream fetchFulltext(String path) throws IOException;

}
