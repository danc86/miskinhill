package au.com.miskinhill.rdftemplate.selector;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SelectorFactory} implementation which indirects to a real
 * implementation and caches its return values eternally. Do not use in
 * situations where the set of input expressions can be unbounded (e.g.
 * user-provided) as this will lead to unbounded cache growth.
 * <p>
 * A better implementation would use a LRU cache or similar, but I cbf.
 */
public class EternallyCachingSelectorFactory implements SelectorFactory {
    
    private final SelectorFactory real;
    private final Map<String, Selector<?>> cache = new HashMap<String, Selector<?>>();
    
    public EternallyCachingSelectorFactory(SelectorFactory real) {
        this.real = real;
    }
    
    @Override
    public Selector<?> get(String expression) {
        Selector<?> cached = cache.get(expression);
        if (cached == null) {
            Selector<?> fresh = real.get(expression);
            cache.put(expression, fresh);
            return fresh;
        } else {
            return cached;
        }
    }

}
