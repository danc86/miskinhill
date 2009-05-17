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
		types.put(Article.TYPE, Article.class);
		types.put(Review.TYPE, Review.class);
		types.put(Author.TYPE, Author.class);
		types.put(Book.TYPE, Book.class);
	}

	public static GenericResource fromRDF(Resource rdfResource, FulltextFetcher fulltextFetcher) {
        if (!rdfResource.isAnon() && !rdfResource.getURI().startsWith("http://miskinhill.com.au"))
            return null;
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
                /*
                 * We attach blank nodes to this document because they won't
                 * appear anywhere else.
                 */
                GenericResource o = GenericResource.fromRDF(
                        (Resource) stmt.getObject().as(Resource.class), fulltextFetcher);
                if (o != null)
                    o.addFieldsToDocument(fieldNamePrefix + stmt.getPredicate().getURI() + " ", doc);
            }
        }
		
		if (!rdfResource.isAnon()) {
    	    doc.add(new Field(fieldNamePrefix + "url", rdfResource.getURI(), 
    	            Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		}
	    doc.add(new Field(fieldNamePrefix + "type", rdfType().getURI(), 
	            Store.YES, Index.NOT_ANALYZED_NO_NORMS));
	}

    /**
     * Returns the anchor text (possibly with markup) to be displayed in search
     * results when linking to this resource.
     */
	protected abstract String getAnchorText();
	
	/**
	 * Returns the (most specific) RDF type associated with this class. 
	 */
	protected abstract Resource rdfType();

	public void addToIndex(IndexWriter iw)
			throws UnknownLiteralTypeException, IOException, XMLStreamException {
		Document doc = new Document();
		addFieldsToDocument("", doc);
        // add anchor text as untokenized and stored, so that search-webapp can fetch it
        doc.add(new Field("anchor", getAnchorText(), Store.YES, Index.NO));
		iw.addDocument(doc);
	}
    
    public static String toHTML(Literal literal) {
        if (literal.getDatatypeURI() == null) {
            /* untyped literal, so needs escaping */
            return literal.getString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        } else if (literal.getDatatypeURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral")) {
            /* XML literal, so return as is */
            return literal.getString();
        } else {
            throw new IllegalArgumentException("Anchor text is neither untyped nor XML literal");
        }
    }

    /**
     * Indicates whether this resource type is expecting to be added as a
     * top-level document, or only added transitively by another resource.
     */
    public abstract boolean isTopLevel();

}
