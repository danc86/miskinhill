package au.com.miskinhill.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.domain.GenericResource;
import au.com.miskinhill.domain.fulltext.FileFulltextFetcher;
import au.com.miskinhill.search.analysis.NullAnalyzer;
import au.com.miskinhill.search.analysis.XMLTokenizer;
import au.com.miskinhill.xhtmldtd.XhtmlEntityResolver;

/**
 * Builds the Lucene index from metadata and content.
 */
public class Indexer {
    
    private static final Logger LOG = Logger.getLogger(Indexer.class.getName());

    private static void writeIndex(final String contentPath, final File indexPath) 
            throws Exception {
        
        XMLTokenizer.getXMLInputFactory().setXMLResolver(new XhtmlEntityResolver());
        
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream(new File(contentPath + "/meta.xml")), "", "RDF/XML");
        model.read(new FileInputStream(new File(contentPath + "/meta-inferred.xml")), "", "RDF/XML");
        
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), 
                NullAnalyzer.INSTANCE, 
                /* create */ true, 
                MaxFieldLength.UNLIMITED);
        try {
    		iw.setUseCompoundFile(false);
    		
    		FileFulltextFetcher fulltextFetcher = new FileFulltextFetcher(contentPath);
    		
    		ResIterator articles = model.listSubjects();
    		while (articles.hasNext()) {
    			GenericResource r = GenericResource.fromRDF(articles.nextResource(), fulltextFetcher);
    			if (r != null && r.isTopLevel()) {
    			    try {
                        r.addToIndex(iw);
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "IOException while indexing " + r, e);
                        throw e;
                    }
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
        writeIndex(props.getProperty("contentPath"),
                new File(props.getProperty("indexPath")));
	}
	
}
