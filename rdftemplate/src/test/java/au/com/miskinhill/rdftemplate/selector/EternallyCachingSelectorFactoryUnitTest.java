package au.com.miskinhill.rdftemplate.selector;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import au.com.miskinhill.rdftemplate.TestNamespacePrefixMap;

public class EternallyCachingSelectorFactoryUnitTest {
    
    @Test
    public void shouldCacheSelectors() {
        AntlrSelectorFactory wrappedFactory = new AntlrSelectorFactory();
        wrappedFactory.setNamespacePrefixMap(TestNamespacePrefixMap.getInstance());
        EternallyCachingSelectorFactory factory = new EternallyCachingSelectorFactory(wrappedFactory);
        Selector<?> first = factory.get("dc:creator/foaf:name");
        Selector<?> second = factory.get("dc:creator/foaf:name");
        assertThat((Selector) first, sameInstance((Selector) second));
    }

}
