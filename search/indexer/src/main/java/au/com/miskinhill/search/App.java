package au.com.miskinhill.search;

import java.io.File;
import java.io.FileInputStream;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
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
		
		Resource articleType = model.getResource("http://miskinhill.com.au/rdfschema/1.0/Article");
		ResIterator articles = model.listSubjectsWithProperty(RDF.type, articleType);
		while (articles.hasNext()) {
			Article a = new Article(articles.nextResource());
			System.out.println(a.toString());
		}
	}
	
}
