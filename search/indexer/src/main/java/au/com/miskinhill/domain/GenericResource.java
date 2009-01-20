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
import com.hp.hpl.jena.rdf.model.Property;
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

	public static GenericResource fromRDF(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		StmtIterator i = rdfResource.listProperties(RDF.type);
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			Resource type = (Resource) stmt.getObject().as(Resource.class);
			if (types.containsKey(type)) {
				try {
					return types.get(type).getConstructor(Resource.class, FulltextFetcher.class)
							.newInstance(rdfResource, fulltextFetcher);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	protected final Resource rdfResource;
    protected final FulltextFetcher fulltextFetcher;

	public GenericResource(Resource rdfResource, FulltextFetcher fulltextFetcher) {
		this.rdfResource = rdfResource;
        this.fulltextFetcher = fulltextFetcher;
	}
	
	public void addFieldsToDocument(String fieldNamePrefix, final Document doc) 
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		StmtIterator i = rdfResource.listProperties();
		while (i.hasNext()) {
			final Statement stmt = i.nextStatement();
			if (stmt.getObject().isLiteral()) {
				doc.add(new Field(fieldNamePrefix + stmt.getPredicate().getURI(), 
						RDFLiteralTokenizer.fromLiteral(
							(Literal) stmt.getObject().as(Literal.class))));
			} else if (stmt.getObject().isAnon()) {
				GenericResource o = GenericResource.fromRDF(
						(Resource) stmt.getObject().as(Resource.class), 
                        fulltextFetcher);
				if (o != null)
					o.addFieldsToDocument(fieldNamePrefix + stmt.getPredicate().getURI() + " ", doc);
			}
		}
		
		// add anchor text as untokenized and stored, so that search-webapp can fetch it
		doc.add(new Field("anchor", 
				rdfResource.getRequiredProperty(anchorProperty()).getString(), 
				Store.YES, Index.NO));
	}

	/**
	 * Returns the RDF {@link Property} whose value is to be used as anchor text
	 * in search results.
	 */
	protected abstract Property anchorProperty();

	public void addToIndex(IndexWriter iw)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		Document doc = new Document();
		doc.add(new Field("url", rdfResource.getURI(), 
				Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		addFieldsToDocument("", doc);
		iw.addDocument(doc);
	}

}
