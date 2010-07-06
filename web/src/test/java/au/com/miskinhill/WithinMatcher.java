package au.com.miskinhill;

import org.hamcrest.Description;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.junit.internal.matchers.TypeSafeMatcher;

public class WithinMatcher<T extends ReadableInstant> extends TypeSafeMatcher<T> {
    
    private final Duration duration;
    
    public WithinMatcher(long millis) {
        duration = new Duration(millis);
    }
    
    public WithinMatcher(Duration duration) {
        this.duration = duration;
    }
    
    @Override
    public boolean matchesSafely(T item) {
        return duration.toIntervalTo(new Instant()).contains(item);
    }

    @Override
    public void describeTo(Description desc) {
        desc.appendText("an instant within ");
        desc.appendValue(duration);
        desc.appendText(" of now");
    }

}
