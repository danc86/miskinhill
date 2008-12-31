package au.com.miskinhill.search;

import java.io.File;
import java.io.FileInputStream;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;

import au.com.miskinhill.domain.Article;
import au.com.miskinhill.domain.vocabulary.MHS;
import au.com.miskinhill.search.analysis.NullAnalyzer;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Hello world!
 * 
 */
public class App {
	
	public static void main(String[] args) throws Exception {
		Model schemaModel = ModelFactory.createDefaultModel();
		schemaModel.read(new FileInputStream(new File("../../rdfschema/schema.ttl")), "", "TURTLE");
		
		Model metadataModel = ModelFactory.createDefaultModel();
		metadataModel.read(new FileInputStream(new File("../../rdf.nt")), "", "N-TRIPLE");
		
		InfModel model = ModelFactory.createRDFSModel(ModelFactory.createUnion(schemaModel, metadataModel));
		
		IndexWriter iw = new IndexWriter(FSDirectory.getDirectory("../index-data"), 
				NullAnalyzer.INSTANCE, 
				/* create */ true, 
				MaxFieldLength.UNLIMITED);
		iw.setUseCompoundFile(false);
		
		ResIterator articles = model.listSubjectsWithProperty(RDF.type, MHS.Article);
		while (articles.hasNext()) {
			Article a = new Article(articles.nextResource());
			a.addToIndex(iw);
		}
		
		iw.commit();
		iw.optimize();
		iw.close();
	}
	
}
