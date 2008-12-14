package au.com.miskinhill.search;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;

public interface Indexable {

	public void addToIndex(IndexWriter iw) throws IOException;
	
}
