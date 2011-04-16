package au.com.miskinhill.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import au.com.miskinhill.rdf.vocabulary.Lexvo;

public final class LanguageUtil {
    
    ///CLOVER:OFF
    private LanguageUtil() {
    }
    ///CLOVER:ON

    /**
     * Given a two- or three-letter language code, returns the corresponding
     * Lexvo language.
     */
    // TODO implement full RFC3066 here
    public static Resource lookupLanguage(Model model, String lang) {
        ResIterator it = model.listResourcesWithProperty(Lexvo.iso639P1Code, lang);
        if (it.hasNext())
            return it.nextResource();
        it = model.listResourcesWithProperty(Lexvo.iso6392BCode, lang);
        if (it.hasNext())
            return it.nextResource();
        return null;
    }

}
