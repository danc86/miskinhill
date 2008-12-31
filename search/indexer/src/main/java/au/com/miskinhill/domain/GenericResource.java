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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
public class GenericResource {
	
	private static Map<Resource, Class<? extends GenericResource>> types = 
			new HashMap<Resource, Class<? extends GenericResource>>();
	static {
		types.put(MHS.Article, Article.class);
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
		// fallback
		return new GenericResource(rdfResource);
	}
	
	protected Resource rdfResource;

	public GenericResource(Resource rdfResource) {
		this.rdfResource = rdfResource;
	}
	
	public void addFieldsToDocument(final Document doc) 
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		doc.add(new Field("url", rdfResource.getURI(), 
				Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		
		StmtIterator i = rdfResource.listProperties();
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			stmt.getObject().visitWith(new RDFVisitor() {
				@Override
				public Object visitBlank(com.hp.hpl.jena.rdf.model.Resource r, AnonId id) {
					/* pass */
					return null;
				}

				@Override
				public Object visitLiteral(Literal literal) {
					try {
						doc.add(new Field(stmt.getPredicate().getURI(), 
								RDFLiteralTokenizer.fromLiteral(literal)));
					} catch (UnknownLiteralTypeException e) {
						throw new RuntimeException(e);
					}
					return null;
				}

				@Override
				public Object visitURI(com.hp.hpl.jena.rdf.model.Resource r, String uri) {
					return null;
				}
				
			});
		}
	}

	public void addToIndex(IndexWriter iw)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		Document doc = new Document();
		addFieldsToDocument(doc);
		iw.addDocument(doc);
	}

}