
package org.n52.prosecco.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.ServiceParameters;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.FilterContext.FilterContextBuilder;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanRelation;

public final class RequestFilterEngineTest {

    // TODO test with empty parameters --> serviceParameters

    @Test
    public void given_allowingValueRestriction_when_contextWithAllowedValues_then_allowedValuesKept() {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "value1", "value2");
        PolicyConfig config = PolicyConfig.createSimple("allow", "role", restriction);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withParameters("phenomenon", "value1", "value2")
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_denyingValueRestriction_when_contextWithAllowedValues_then_deniedValuesRemoved() {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "value1", "value2");
        PolicyConfig config = PolicyConfig.createSimple("role", restriction);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withParameters("phenomenon", "value1", "value2")
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).doesNotContain("value1", "value2");
    }

    @Test
    public void given_simpleConfig_when_contextWithHasUnconfiguredValues_then_unconfiguredValuesRemoved() {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "value1");
        PolicyConfig config = PolicyConfig.createSimple("allow", "role", restriction);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withParameters("phenomenon", "value1", "value2")
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsExactly("value1");
    }

    @Test
    public void given_phenomenaFilter_when_contextWithAllowedAndDeniedValues_then_allowedValuesKeptOnly() {
        ValueRestriction valueRestriction1 = ValueRestriction.of("phenomenon", "value1", "value2");
        ValueRestriction valueRestriction2 = ValueRestriction.of("phenomenon", "restrictedValue");
        Policy allowingPolicy = Policy.of("allowing-policy", "allow", valueRestriction1);
        Policy denyingPolicy = Policy.of("denying-policy", valueRestriction2);
        List<Policy> policies = Arrays.asList(allowingPolicy, denyingPolicy);

        List<String> roles = Collections.singletonList("role");
        Rule rule = Rule.of("rule1", roles, "allowing-policy", "denying-policy");
        List<Rule> rules = Collections.singletonList(rule);

        PolicyConfig config = new PolicyConfig(policies, rules);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withParameters("phenomenon", "value1", "restrictedValue")
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsExactly("value1");
    }

    @Test
    public void given_cachedServiceParameters_when_contextIsEmpty_then_allowedValuesAddedOnly() {
        ValueRestriction valueRestriction1 = ValueRestriction.of("phenomenon", "value1", "value2");
        ValueRestriction valueRestriction2 = ValueRestriction.of("phenomenon", "restricted");
        Policy allowingPolicy = Policy.of("allowing-policy", "allow", valueRestriction1);
        Policy denyingPolicy = Policy.of("denying-policy", valueRestriction2);
        List<Policy> policies = Arrays.asList(allowingPolicy, denyingPolicy);

        List<String> roles = Collections.singletonList("role");
        Rule rule = Rule.of("rule1", roles, "allowing-policy", "denying-policy");
        List<Rule> rules = Collections.singletonList(rule);

        PolicyConfig config = new PolicyConfig(policies, rules);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        // simulate an empty request and having cached service parameters
        ServiceParameters serviceParameters = new ServiceParameters("phenomenon", "value1", "value2", "restricted");
        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withServiceParameters(serviceParameters)
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_restrictedFloatingTime_when_fullyIncludingBetween_then_timespanBeforeStartRestriction() {
        ValueRestriction timeRestriction = ValueRestriction.of("timespan", "floating,P7D");
        Policy denyingPolicy = Policy.of("denying-policy", timeRestriction);

        List<String> roles = Collections.singletonList("role");
        Rule rule = Rule.of("rule1", roles, "allowing-policy", "denying-policy");

        PolicyConfig config = new PolicyConfig(denyingPolicy, rule);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        Instant end = Instant.now();
        Instant start = end.minus(4, ChronoUnit.DAYS);
        Timespan queriedTimespan = Timespan.between(start, end);
        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withTimespans(queriedTimespan)
                                                           .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        Optional<Timespan> timespan = evaluatedContext.getFirstTimespan();
        Timespan actual = timespan.get();

        assertThat(actual.getRelation()).isEqualTo(TimespanRelation.BEFORE);
        assertThat(Instant.from(actual.getEnd())).satisfies(e -> {
            Instant expectedEnd = end.minus(7, ChronoUnit.DAYS);
            assertThat(e).isBefore(expectedEnd.plus(1, ChronoUnit.SECONDS));
        });
    }
}
