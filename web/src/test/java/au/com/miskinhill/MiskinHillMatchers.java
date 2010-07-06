package au.com.miskinhill;

import java.util.Collection;

import org.hamcrest.Matcher;
import org.joda.time.Duration;
import org.joda.time.ReadableInstant;

public final class MiskinHillMatchers {
    
    public static <T extends Comparable<T>> Matcher<T> greaterThan(T target) {
        return new GreaterThanMatcher<T>(target);
    }
    
    public static <T extends Comparable<T>> Matcher<? extends Collection<T>> decreasingOrder(Class<T> clazz) {
        return new DecreasingOrderMatcher<T>();
    }
    
    public static <T extends ReadableInstant> Matcher<T> within(Duration duration) {
        return new WithinMatcher<T>(duration);
    }
    
    public static <T extends ReadableInstant> Matcher<T> within(long millis) {
        return new WithinMatcher<T>(millis);
    }

}
