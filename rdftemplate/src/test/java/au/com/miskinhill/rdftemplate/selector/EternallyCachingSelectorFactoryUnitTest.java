package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EternallyCachingSelectorFactoryUnitTest {
    
    @Test
    public void shouldCacheSelectors() {
        EternallyCachingSelectorFactory factory = new EternallyCachingSelectorFactory(new AntlrSelectorFactory());
        Selector<?> first = factory.get("dc:creator/foaf:name");
        Selector<?> second = factory.get("dc:creator/foaf:name");
        assertThat((Selector) first, sameInstance((Selector) second));
    }

}
