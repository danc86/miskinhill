package au.com.miskinhill.domain;

import org.hamcrest.Description;

import org.apache.lucene.document.Field;

import org.junit.internal.matchers.TypeSafeMatcher;

public final class FieldMatcher {
    
    private FieldMatcher() {
    }
    
    private static final class IndexedUnstoredWithName extends TypeSafeMatcher<Field> {
        
        private final String name;
        
        public IndexedUnstoredWithName(String name) {
            this.name = name;
        }
        
        @Override
        public boolean matchesSafely(Field field) {
            return field.name().equals(name) && !field.isStored() && field.isIndexed();
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText("an indexed, unstored Field with name [" + name + "]");
        }
        
    }
    
    private static final class StoredIndexedWithNameAndValue extends TypeSafeMatcher<Field> {
        
        private final String name;
        private final String value;
        
        public StoredIndexedWithNameAndValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public boolean matchesSafely(Field field) {
            return field.name().equals(name) && field.stringValue().equals(value) && field.isStored() && field.isIndexed();
        }
        
        @Override
        public void describeTo(Description desc) {
            desc.appendText("a stored, indexed Field with name [" + name + "] and value [" + value + "]");
        }
        
    }
    
    public static IndexedUnstoredWithName indexedUnstoredFieldWithName(String name) {
        return new IndexedUnstoredWithName(name);
    }
    
    public static StoredIndexedWithNameAndValue storedIndexedFieldWithNameAndValue(String name, String value) {
        return new StoredIndexedWithNameAndValue(name, value);
    }

}
