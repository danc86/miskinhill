package au.com.miskinhill.rdftemplate.selector;

public interface SelectorFactory {
    
    Selector<?> get(String expression);

}
