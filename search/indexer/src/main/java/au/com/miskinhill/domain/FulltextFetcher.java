package au.com.miskinhill.domain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for loading full-text content of articles and reviews from the
 * filesystem.
 * 
 * Mainly exists so that it can be mocked out in tests.
 */
public class FulltextFetcher {
	
	private final String basePath;
	
	public FulltextFetcher(String basePath) {
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		this.basePath = basePath;
	}
	
	public InputStream fetch(String path) throws IOException {
		return new BufferedInputStream(new FileInputStream(
				new File(this.basePath + path)));
	}

}
