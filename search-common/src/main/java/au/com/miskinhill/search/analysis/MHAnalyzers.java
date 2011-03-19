package au.com.miskinhill.search.analysis;

public final class MHAnalyzers {
    
    private MHAnalyzers() {
    }
    
    private static final PerLanguageAnalyzerMap ANALYZER_MAP = new PerLanguageAnalyzerMap(new DefaultAnalyzer());
    static {
        ANALYZER_MAP.addAnalyzer("en", new EnglishAnalyzer());
        ANALYZER_MAP.addAnalyzer("ru", new RussianAnalyzer());
        ANALYZER_MAP.addAnalyzer("de", new GermanAnalyzer());
        ANALYZER_MAP.addAnalyzer("fr", new FrenchAnalyzer());
    }
    public static PerLanguageAnalyzerMap getAnalyzerMap() {
        return ANALYZER_MAP;
    }

}
