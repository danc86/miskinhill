package au.com.miskinhill.rdftemplate.selector;

public interface AdaptationResolver {
    
    <T> Class<? extends Adaptation<?>> getByName(String name);

}
