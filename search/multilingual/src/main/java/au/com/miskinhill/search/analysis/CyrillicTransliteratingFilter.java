package au.com.miskinhill.search.analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Assumes that tokens have already been lower-cased.
 */
public class CyrillicTransliteratingFilter extends TokenFilter {
    
    private static final String CYRILLIC_PATTERN = ".*[а-я]+.*";
    
    private Token transliterated = null;
    
    protected CyrillicTransliteratingFilter(TokenStream input) {
        super(input);
    }
    
    @Override
    public Token next(Token reusableToken) throws IOException {
        Token tok;
        if (transliterated == null) {
            tok = input.next(reusableToken);
            if (tok == null) return null;
            if (needsTransliterating(tok.term())) {
                transliterated = (Token) tok.clone();
                transliterated.setTermBuffer(transliterate(transliterated.term()));
                transliterated.setPositionIncrement(0);
            }
        } else {
            tok = transliterated;
            transliterated = null;
        }
        return tok;
    }
    
    private static boolean needsTransliterating(String text) {
        return (text.matches(CYRILLIC_PATTERN));
    }
    
    private static final Map<Character, String> TRANSLITERATION_TABLE = new HashMap<Character, String>();
    static {
        TRANSLITERATION_TABLE.put('а', "a");
        TRANSLITERATION_TABLE.put('б', "b");
        TRANSLITERATION_TABLE.put('в', "v");
        TRANSLITERATION_TABLE.put('г', "g");
        TRANSLITERATION_TABLE.put('д', "d");
        TRANSLITERATION_TABLE.put('е', "e");
        TRANSLITERATION_TABLE.put('ё', "e");
        TRANSLITERATION_TABLE.put('ж', "zh");
        TRANSLITERATION_TABLE.put('з', "z");
        TRANSLITERATION_TABLE.put('и', "i");
        TRANSLITERATION_TABLE.put('й', "y");
        TRANSLITERATION_TABLE.put('к', "k");
        TRANSLITERATION_TABLE.put('л', "l");
        TRANSLITERATION_TABLE.put('м', "m");
        TRANSLITERATION_TABLE.put('н', "n");
        TRANSLITERATION_TABLE.put('о', "o");
        TRANSLITERATION_TABLE.put('п', "p");
        TRANSLITERATION_TABLE.put('р', "r");
        TRANSLITERATION_TABLE.put('с', "s");
        TRANSLITERATION_TABLE.put('т', "t");
        TRANSLITERATION_TABLE.put('у', "u");
        TRANSLITERATION_TABLE.put('ф', "f");
        TRANSLITERATION_TABLE.put('х', "kh");
        TRANSLITERATION_TABLE.put('ц', "ts");
        TRANSLITERATION_TABLE.put('ч', "ch");
        TRANSLITERATION_TABLE.put('ш', "sh");
        TRANSLITERATION_TABLE.put('щ', "shch");
        TRANSLITERATION_TABLE.put('ъ', "'");
        TRANSLITERATION_TABLE.put('ы', "y");
        TRANSLITERATION_TABLE.put('ь', "'");
        TRANSLITERATION_TABLE.put('э', "e");
        TRANSLITERATION_TABLE.put('ю', "iu");
        TRANSLITERATION_TABLE.put('я', "ia");
    }
    
    private static String transliterate(CharSequence cyrillic) {
        StringBuilder transliterated = new StringBuilder();
        for (int i = 0; i < cyrillic.length(); i ++) {
            Character c = cyrillic.charAt(i);
            if (TRANSLITERATION_TABLE.containsKey(c))
                transliterated.append(TRANSLITERATION_TABLE.get(c));
            else 
                transliterated.append(c);
        }
        return transliterated.toString();
    }

}
