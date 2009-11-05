package au.com.miskinhill.search;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.domain.FulltextFetcher;
import au.com.miskinhill.domain.GenericResource;
import au.com.miskinhill.search.analysis.NullAnalyzer;

/**
 * Builds the Lucene index from metadata and content.
 */
public class Indexer {

    private static void writeIndex(final String contentPath, final String indexPath) 
            throws Exception {
        
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream(new File(contentPath + "/meta.xml")), "", "RDF/XML");
        
        IndexWriter iw = new IndexWriter(FSDirectory.getDirectory(indexPath), 
                NullAnalyzer.INSTANCE, 
                /* create */ true, 
                MaxFieldLength.UNLIMITED);
        try {
    		iw.setUseCompoundFile(false);
    		
    		FulltextFetcher fulltextFetcher = new FulltextFetcher(contentPath);
    		
    		ResIterator articles = model.listSubjects();
    		while (articles.hasNext()) {
    			GenericResource r = GenericResource.fromRDF(articles.nextResource(), fulltextFetcher);
    			if (r != null && r.isTopLevel()) {
    			    r.addToIndex(iw);
    			}
    		}
		
    		iw.commit();
    		iw.optimize();
        } finally {
            iw.close();
        }
    }
	
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(Indexer.class.getResourceAsStream("paths.properties"));
        writeIndex(props.getProperty("contentPath"), props.getProperty("indexPath"));
	}
	
}
