package au.com.miskinhill;

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

public class GreaterThanMatcher<T extends Comparable<T>> extends TypeSafeMatcher<T> {
    
    private final T target;
    
    public GreaterThanMatcher(T target) {
        this.target = target;
    }

    @Override
    public boolean matchesSafely(T item) {
        return item.compareTo(target) > 0;
    }

    @Override
    public void describeTo(Description desc) {
        desc.appendText("greater than");
        desc.appendValue(target);
    }

}
