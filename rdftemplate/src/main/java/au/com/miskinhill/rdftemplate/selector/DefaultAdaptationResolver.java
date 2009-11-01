package au.com.miskinhill.rdftemplate.selector;

import java.util.HashMap;
import java.util.Map;

public class DefaultAdaptationResolver implements AdaptationResolver {
    
    private static final Map<String, Class<? extends Adaptation<?>>> ADAPTATIONS = new HashMap<String, Class<? extends Adaptation<?>>>();
    static {
        ADAPTATIONS.put("uri", UriAdaptation.class);
        ADAPTATIONS.put("uri-slice", UriSliceAdaptation.class);
        ADAPTATIONS.put("lv", LiteralValueAdaptation.class);
        ADAPTATIONS.put("comparable-lv", ComparableLiteralValueAdaptation.class);
        ADAPTATIONS.put("formatted-dt", FormattedDateTimeAdaptation.class);
    }

    @Override
    public Class<? extends Adaptation<?>> getByName(String name) {
        return ADAPTATIONS.get(name);
    }

}
