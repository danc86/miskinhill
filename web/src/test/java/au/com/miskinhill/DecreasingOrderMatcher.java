package au.com.miskinhill;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

public class DecreasingOrderMatcher<T extends Comparable<T>> extends TypeSafeMatcher<Collection<T>> {
    
    @Override
    public boolean matchesSafely(Collection<T> item) {
        if (item.size() < 2) return true;
        Iterator<? extends T> it = item.iterator();
        T previous = it.next();
        while (it.hasNext()) {
            T current = it.next();
            if (current.compareTo(previous) > 0) return false;
            previous = current;
        }
        return true;
    }

    @Override
    public void describeTo(Description desc) {
        desc.appendText("elements in decreasing order");
    }

}
