package au.com.miskinhill.rdftemplate.selector;


import static org.hamcrest.CoreMatchers.equalTo;

public class AdaptationMatcher<T extends Adaptation<?>> extends BeanPropertyMatcher<T> {
    
    private AdaptationMatcher(Class<T> type) {
        super(type);
    }
    
    public static AdaptationMatcher<UriAdaptation> uriAdaptation() {
        return new AdaptationMatcher<UriAdaptation>(UriAdaptation.class);
    }
    
    public static AdaptationMatcher<UriSliceAdaptation> uriSliceAdaptation(Integer startIndex) {
        AdaptationMatcher<UriSliceAdaptation> m = new AdaptationMatcher<UriSliceAdaptation>(UriSliceAdaptation.class);
        m.addRequiredProperty("startIndex", equalTo(startIndex));
        return m;
    }
    
    public static AdaptationMatcher<LiteralValueAdaptation> lvAdaptation() {
        return new AdaptationMatcher<LiteralValueAdaptation>(LiteralValueAdaptation.class);
    }
    
    public static AdaptationMatcher<ComparableLiteralValueAdaptation> comparableLVAdaptation() {
        return new AdaptationMatcher<ComparableLiteralValueAdaptation>(ComparableLiteralValueAdaptation.class);
    }
    
    public static AdaptationMatcher<FormattedDateTimeAdaptation> formattedDTAdaptation(String pattern) {
        AdaptationMatcher<FormattedDateTimeAdaptation> m = new AdaptationMatcher<FormattedDateTimeAdaptation>(FormattedDateTimeAdaptation.class);
        m.addRequiredProperty("pattern", equalTo(pattern));
        return m;
    }

}
