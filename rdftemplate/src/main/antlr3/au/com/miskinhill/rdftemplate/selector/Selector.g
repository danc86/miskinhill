grammar Selector;

@parser::header {
package au.com.miskinhill.rdftemplate.selector;
}

@parser::members {
    public static Selector<?> parse(String expression) {
        CharStream stream = new ANTLRStringStream(expression);
        SelectorLexer lexer = new SelectorLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SelectorParser parser = new SelectorParser(tokens);
        try {
            return parser.selector();
        } catch (RecognitionException e) {
            throw new InvalidSelectorSyntaxException(e);
        }
    }
    
    @Override
    public void reportError(RecognitionException e) {
        throw new InvalidSelectorSyntaxException(e);
    }
}

@lexer::header {
package au.com.miskinhill.rdftemplate.selector;
}

@lexer::members {
    @Override
    public void reportError(RecognitionException e) {
        throw new InvalidSelectorSyntaxException(e);
    }
}

start : selector ;

selector returns [Selector<?> result]
    : ( ts=traversingSelector { result = ts; }
      | { result = new NoopSelector(); }
      )
      ( '#'
        ( URI_ADAPTATION { result = new SelectorWithAdaptation(result, new UriAdaptation()); }
        | URI_SLICE_ADAPTATION
          '('
          startIndex=INTEGER { result = new SelectorWithAdaptation(result,
                                       new UriSliceAdaptation(Integer.parseInt($startIndex.text))); }
          ')'
        | COMPARABLE_LV_ADAPTATION { result = new SelectorWithAdaptation(result, new ComparableLiteralValueAdaptation()); }
        | LV_ADAPTATION { result = new SelectorWithAdaptation(result, new LiteralValueAdaptation()); }
        )
      |
      )
    ;

traversingSelector returns [TraversingSelector result]
@init {
    result = new TraversingSelector();
}
    : t=traversal { $result.addTraversal(t); }
      ( '/'
        t=traversal { $result.addTraversal(t); }
      ) *
    ;
    
traversal returns [Traversal result]
@init {
    result = new Traversal();
}
    : ( '!' { $result.setInverse(true); }
      | // optional
      )
      nsprefix=XMLTOKEN { $result.setPropertyNamespacePrefix($nsprefix.text); }
      ':'
      localname=XMLTOKEN { $result.setPropertyLocalName($localname.text); }
      ( '['
        URI_PREFIX_PREDICATE
        '='
        uriPrefix=SINGLE_QUOTED { $result.setPredicate(new UriPrefixPredicate($uriPrefix.text)); }
        ']'
      | // optional
      )
      ( '('
        ( '~' { $result.setReverseSorted(true); }
        | // optional
        )
        s=selector { $result.setSortOrder(s.withResultType(Comparable.class)); }
        ')'
      | // optional
      )
      ( '['
        subscript=INTEGER { $result.setSubscript(Integer.parseInt($subscript.text)); }
        ']'
      | // optional
      )
    ;

URI_PREFIX_PREDICATE : 'uri-prefix' ;
FIRST_PREDICATE : 'first' ;
LV_ADAPTATION : 'lv' ;
COMPARABLE_LV_ADAPTATION : 'comparable-lv' ;
URI_SLICE_ADAPTATION : 'uri-slice' ;
URI_ADAPTATION : 'uri' ;

XMLTOKEN : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')* ;
INTEGER : ('0'..'9')+ ;
SINGLE_QUOTED : '\'' ( options {greedy=false;} : . )* '\''
    {
        // strip quotes
        String txt = getText();
        setText(txt.substring(1, txt.length() -1));
    };