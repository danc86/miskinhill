package au.com.miskinhill.search;

import org.apache.lucene.wordnet.Syns2Index;

/**
 * Builds the synonyms index from WordNet data.
 */
public class SynonymsIndexer {

	public static void main(String[] args) throws Throwable {
		Syns2Index.main(new String[] {
				SynonymsIndexer.class.getResource("wn_s.pl").getPath(), 
                System.getProperty("au.com.miskinhill.indexPath") + "/synonyms"});
	}
	
}
