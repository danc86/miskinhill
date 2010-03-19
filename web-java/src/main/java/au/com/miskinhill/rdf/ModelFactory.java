package au.com.miskinhill.rdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Model;

public final class ModelFactory {
	
	private static final Logger LOG = Logger.getLogger(ModelFactory.class.getName());
	
	///CLOVER:OFF
	private ModelFactory() {
	}
	///CLOVER:ON
	
	public static Model load(String filename) throws IOException {
		Model m = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
		LOG.info("Reading RDF model from " + filename);
		m.read(new FileInputStream(filename), null);
		return m;
	}
	
	public static Model load(Class<?> clazz, String resourcePath) throws IOException {
	    Model m = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
	    LOG.info("Reading RDF model from " + clazz.getResource(resourcePath));
	    InputStream stream = clazz.getResourceAsStream(resourcePath);
        try {
            m.read(stream, null);
        } finally {
            stream.close();
        }
	    return m;
	}

}
