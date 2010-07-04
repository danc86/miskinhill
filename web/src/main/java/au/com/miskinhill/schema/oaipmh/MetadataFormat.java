package au.com.miskinhill.schema.oaipmh;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {
    "metadataPrefix",
    "schema",
    "metadataNamespace"
})
public class MetadataFormat {

    @XmlElement(required = true)
    private String metadataPrefix;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    private String schema;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    private String metadataNamespace;
    
    protected MetadataFormat() {
    }
    
    public MetadataFormat(String metadataPrefix, URI schema, URI metadataNamespace) {
        this.metadataPrefix = metadataPrefix;
        this.schema = schema.toString();
        this.metadataNamespace = metadataNamespace.toString();
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public String getSchema() {
        return schema;
    }

    public String getMetadataNamespace() {
        return metadataNamespace;
    }

}
