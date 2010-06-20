package au.com.miskinhill;

import java.util.Collection;

import org.hamcrest.Matcher;

public final class MiskinHillMatchers {
    
    public static <T extends Comparable<T>> Matcher<T> greaterThan(T target) {
        return new GreaterThanMatcher<T>(target);
    }
    
    public static <T extends Comparable<T>> Matcher<? extends Collection<T>> decreasingOrder(Class<T> clazz) {
        return new DecreasingOrderMatcher<T>();
    }

}
