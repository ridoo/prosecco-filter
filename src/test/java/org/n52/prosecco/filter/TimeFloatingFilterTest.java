package org.n52.prosecco.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanParser;

public class TimeFloatingFilterTest {

    @Test
    public void test() {
        
        Instant now = Instant.now();
        Instant start = now.minus(8, ChronoUnit.DAYS);
        Instant end = now.minus(1, ChronoUnit.DAYS);
        
        ValueRestriction valueRestriction = ValueRestriction.of("timespan", "floating,P2D");
        Timespan restrictedTimespan = new TimespanParser().parseTimeRestriction("floating,P2D");
        
        PolicyConfig policyConfig = PolicyConfig.createSimple("role", valueRestriction);
        FilterContext context = FilterContext.create("sos", "role").build();
        TimeFloatingFilter filter = new TimeFloatingFilterSeam(context, policyConfig, restrictedTimespan);

        Timespan timespan = Timespan.between(start, end);
        Set<Timespan> filtered = filter.evaluate("timespan", Collections.singleton(timespan));
        assertThat(filtered).allSatisfy(t -> {
            assertThat(t.getStart()).isEqualTo(start);
            assertThat(t.getEnd()).isEqualTo(restrictedTimespan.getStart());
        });
    }
    
    private static class TimeFloatingFilterSeam extends TimeFloatingFilter {

        private final Timespan restrictedTimespan;

        public TimeFloatingFilterSeam(FilterContext context, PolicyConfig config, Timespan restrictedTimespan) {
            super(context, config);
            this.restrictedTimespan = restrictedTimespan;
        }

        @Override
        protected Timespan parseFloatingRestriction(Optional<String> firstRestriction) {
            return restrictedTimespan;
        }
        
        
        
    }
}
