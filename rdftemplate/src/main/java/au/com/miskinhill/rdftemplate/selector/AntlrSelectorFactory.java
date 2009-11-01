package au.com.miskinhill.rdftemplate.selector;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

public class AntlrSelectorFactory implements SelectorFactory {
    
    private final AdaptationResolver adaptationResolver;
    private final PredicateResolver predicateResolver;
    
    public AntlrSelectorFactory() {
        this.adaptationResolver = new DefaultAdaptationResolver();
        this.predicateResolver = new DefaultPredicateResolver();
    }
    
    public AntlrSelectorFactory(AdaptationResolver adaptationResolver) {
        this.adaptationResolver = adaptationResolver;
        this.predicateResolver = new DefaultPredicateResolver();
    }
    
    public AntlrSelectorFactory(PredicateResolver predicateResolver) {
        this.adaptationResolver = new DefaultAdaptationResolver();
        this.predicateResolver = predicateResolver;
    }
    
    public AntlrSelectorFactory(AdaptationResolver adaptationResolver, PredicateResolver predicateResolver) {
        this.adaptationResolver = adaptationResolver;
        this.predicateResolver = predicateResolver;
    }
    
    @Override
    public Selector<?> get(String expression) {
        CharStream stream = new ANTLRStringStream(expression);
        SelectorLexer lexer = new SelectorLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SelectorParser parser = new SelectorParser(tokens);
        parser.setAdaptationResolver(adaptationResolver);
        parser.setPredicateResolver(predicateResolver);
        try {
            return parser.unionSelector();
        } catch (RecognitionException e) {
            throw new InvalidSelectorSyntaxException(e);
        }
    }

}
