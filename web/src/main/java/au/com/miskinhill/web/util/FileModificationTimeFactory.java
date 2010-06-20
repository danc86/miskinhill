package au.com.miskinhill.web.util;

import java.io.File;

import org.joda.time.DateTime;

public final class FileModificationTimeFactory {
    
    public static DateTime getModificationTimeForPath(String path) {
        return new DateTime(new File(path).lastModified());
    }
    
    private FileModificationTimeFactory() {
    }
    
}
