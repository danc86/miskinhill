package au.com.miskinhill.schema.oaipmh;

public abstract class Response {
    
    protected final Verb verb;
    
    protected Response() {
        this.verb = null;
    }
    
    public Response(Verb verb) {
        this.verb = verb;
    }
    
    public Verb getVerb() {
        return verb;
    }

}
