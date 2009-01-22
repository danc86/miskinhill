package au.com.miskinhill.search;

import java.util.Properties;

import org.apache.lucene.wordnet.Syns2Index;

/**
 * Builds the synonyms index from WordNet data.
 */
public class SynonymsIndexer {

	public static void main(String[] args) throws Throwable {
		Properties props = new Properties();
		props.load(SynonymsIndexer.class.getResourceAsStream("paths.properties"));
		Syns2Index.main(new String[] {
				SynonymsIndexer.class.getResource("wn_s.pl").getPath(), 
				props.getProperty("indexPath") + "/synonyms"});
	}
	
}
