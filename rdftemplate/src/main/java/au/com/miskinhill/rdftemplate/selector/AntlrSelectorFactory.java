package au.com.miskinhill.rdftemplate.selector;

import java.util.Collections;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

public class AntlrSelectorFactory implements SelectorFactory {
    
    private AdaptationResolver adaptationResolver = new DefaultAdaptationResolver();
    private PredicateResolver predicateResolver = new DefaultPredicateResolver();
    private Map<String, String> namespacePrefixMap = Collections.emptyMap();
    
    public AntlrSelectorFactory() {
    }
    
    public void setAdaptationResolver(AdaptationResolver adaptationResolver) {
        this.adaptationResolver = adaptationResolver;
    }
    
    public void setPredicateResolver(PredicateResolver predicateResolver) {
        this.predicateResolver = predicateResolver;
    }
    
    public void setNamespacePrefixMap(Map<String, String> namespacePrefixMap) {
        this.namespacePrefixMap = namespacePrefixMap;
    }
    
    @Override
    public Selector<?> get(String expression) {
        CharStream stream = new ANTLRStringStream(expression);
        SelectorLexer lexer = new SelectorLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SelectorParser parser = new SelectorParser(tokens);
        parser.setAdaptationResolver(adaptationResolver);
        parser.setPredicateResolver(predicateResolver);
        parser.setNamespacePrefixMap(namespacePrefixMap);
        try {
            return parser.unionSelector();
        } catch (RecognitionException e) {
            throw new InvalidSelectorSyntaxException(e);
        }
    }

}
