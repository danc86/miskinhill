package au.com.miskinhill.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import au.com.miskinhill.domain.vocabulary.MHS;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer;
import au.com.miskinhill.search.analysis.RDFLiteralTokenizer.UnknownLiteralTypeException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class GenericResource {
	
	private static Map<Resource, Class<? extends GenericResource>> types = 
			new HashMap<Resource, Class<? extends GenericResource>>();
	static {
		types.put(MHS.Article, Article.class);
		types.put(MHS.Author, Author.class);
	}

	public static GenericResource fromRDF(Resource rdfResource) {
		StmtIterator i = rdfResource.listProperties(RDF.type);
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			Resource type = (Resource) stmt.getObject().as(Resource.class);
			if (types.containsKey(type)) {
				try {
					return types.get(type).getConstructor(Resource.class)
							.newInstance(rdfResource);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	protected Resource rdfResource;

	public GenericResource(Resource rdfResource) {
		this.rdfResource = rdfResource;
	}
	
	public void addFieldsToDocument(final Document doc) 
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		StmtIterator i = rdfResource.listProperties();
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			if (stmt.getObject().isLiteral()) {
				doc.add(new Field(stmt.getPredicate().getURI(), 
						RDFLiteralTokenizer.fromLiteral(
							(Literal) stmt.getObject().as(Literal.class))));
			} else {
				GenericResource o = GenericResource.fromRDF(
						(Resource) stmt.getObject().as(Resource.class));
				if (o != null)
					o.addFieldsToDocument(doc);
			}
		}
	}

	public void addToIndex(IndexWriter iw)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		Document doc = new Document();
		doc.add(new Field("url", rdfResource.getURI(), 
				Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		addFieldsToDocument(doc);
		iw.addDocument(doc);
	}

}