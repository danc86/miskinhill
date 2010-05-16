package au.com.miskinhill.rdf;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.selector.Adaptation;
import au.id.djc.rdftemplate.selector.AdaptationFactory;
import au.id.djc.rdftemplate.selector.DefaultAdaptationFactory;
import au.id.djc.rdftemplate.selector.InvalidSelectorSyntaxException;

@Component("adaptationFactory")
public class MiskinHillAdaptationFactory implements AdaptationFactory {
    
    private final AdaptationFactory defaults = new DefaultAdaptationFactory();
    private final Map<String, Class<? extends Adaptation<?>>> adaptations = new HashMap<String, Class<? extends Adaptation<?>>>();
    private final ApplicationContext applicationContext;
    
    @Autowired
    public MiskinHillAdaptationFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        adaptations.put("representation-anchors", RepresentationAnchorsAdaptation.class);
        adaptations.put("representation-links", RepresentationLinksAdaptation.class);
        adaptations.put("representation-atom-links", RepresentationAtomLinksAdaptation.class);
        adaptations.put("year", YearAdaptation.class);
        adaptations.put("html", HTMLFragmentRepresentationAdaptation.class);
        adaptations.put("mods", MODSRepresentationAdaptation.class);
        adaptations.put("book-links", BookLinksAdaptation.class);
        adaptations.put("article-links", ArticleLinksAdaptation.class);
        adaptations.put("content", ContentAdaptation.class);
        adaptations.put("lcsh-cleanup", LCSHCleanupAdaptation.class);
        adaptations.put("issue-number", IssueNumberAdaptation.class);
    }
    
    @Override
    public boolean hasName(String name) {
        return defaults.hasName(name) || adaptations.containsKey(name);
    }

    @Override
    public Adaptation<?> getByName(String name) {
        if (defaults.hasName(name)) {
            return defaults.getByName(name);
        }
        Class<? extends Adaptation<?>> adaptationClass = adaptations.get(name);
        if (adaptationClass == null) {
            throw new InvalidSelectorSyntaxException("No adaptation named " + name);
        }
        return applicationContext.getBean(adaptationClass);
    }

}
