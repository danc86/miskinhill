package au.com.miskinhill.schema.sitemaps;

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class SitemapsNamespacePrefixMapper extends NamespacePrefixMapper {
    
    private final Map<String, String> prefixes = new HashMap<String, String>();
    
    public SitemapsNamespacePrefixMapper() {
        prefixes.put("http://sw.deri.org/2007/07/sitemapextension/scschema.xsd", "sc");
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        return prefixes.get(namespaceUri);
    }

}
