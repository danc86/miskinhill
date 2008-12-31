package au.com.miskinhill.domain;

import org.apache.lucene.index.IndexWriter;

public interface Indexable {

	public void addToIndex(IndexWriter iw) throws Exception;
	
}
