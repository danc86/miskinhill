package au.com.miskinhill.search;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.domain.FulltextFetcher;
import au.com.miskinhill.domain.GenericResource;
import au.com.miskinhill.search.analysis.NullAnalyzer;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;

/**
 * Hello world!
 * 
 */
public class App {

    private static void writeIndex(final String contentPath, final String indexPath) 
            throws Exception {
        
        Model schemaModel = ModelFactory.createDefaultModel();
        schemaModel.read(new FileInputStream(new File(contentPath + "/rdfschema/schema.ttl")), "", "TURTLE");
        
        Model metadataModel = ModelFactory.createDefaultModel();
        metadataModel.read(new FileInputStream(new File(contentPath + "/meta.nt")), "", "N-TRIPLE");
        
        InfModel model = ModelFactory.createRDFSModel(ModelFactory.createUnion(schemaModel, metadataModel));
        
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
    			if (r != null) {
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
		props.load(App.class.getResourceAsStream("paths.properties"));
        writeIndex(props.getProperty("contentPath"), props.getProperty("indexPath"));
	}
	
}
