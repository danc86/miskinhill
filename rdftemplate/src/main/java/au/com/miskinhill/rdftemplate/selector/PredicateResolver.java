package au.com.miskinhill.rdftemplate.selector;

public interface PredicateResolver {
    
    Class<? extends Predicate> getByName(String name);

}
