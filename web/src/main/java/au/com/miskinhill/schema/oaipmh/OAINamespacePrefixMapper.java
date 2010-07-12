package au.com.miskinhill.schema.oaipmh;

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class OAINamespacePrefixMapper extends NamespacePrefixMapper {
    
    private final Map<String, String> prefixes = new HashMap<String, String>();
    
    public OAINamespacePrefixMapper() {
        prefixes.put("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc");
        prefixes.put("http://purl.org/dc/elements/1.1/", "dc");
        prefixes.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        prefixes.put("http://www.w3.org/XML/1998/namespace", "xml");
        prefixes.put("http://www.loc.gov/mods/v3", "mods");
        prefixes.put("http://www.loc.gov/MARC21/slim", "marc");
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        return prefixes.get(namespaceUri);
    }

}
