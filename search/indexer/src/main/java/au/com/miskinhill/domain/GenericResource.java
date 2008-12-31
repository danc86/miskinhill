package au.com.miskinhill.domain;

import java.util.HashMap;
import java.util.Map;

import au.com.miskinhill.domain.vocabulary.MHS;

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

}