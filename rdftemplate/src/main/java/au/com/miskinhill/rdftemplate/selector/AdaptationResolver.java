package au.com.miskinhill.rdftemplate.selector;

public interface AdaptationResolver {
    
    Class<? extends Adaptation<?>> getByName(String name);

}
