package au.com.miskinhill.web.oaipmh;

public enum Argument {
    
    VERB("verb"),
    IDENTIFIER("identifier"),
    METADATA_PREFIX("metadataPrefix"),
    FROM("from"),
    UNTIL("until"),
    SET("set");
    
    private final String protocolValue;
    
    private Argument(String protocolValue) {
        this.protocolValue = protocolValue;
    }
    
    public String getProtocolValue() {
        return protocolValue;
    }

}
