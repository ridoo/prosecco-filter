
package org.n52.prosecco.engine.eval;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.n52.prosecco.engine.filter.FilterContext;
import org.n52.prosecco.engine.filter.FilterContext.FilterContextBuilder;
import org.n52.prosecco.engine.filter.Timespan;
import org.n52.prosecco.engine.filter.TimespanRelation;
import org.n52.prosecco.engine.policy.Policy;
import org.n52.prosecco.engine.policy.PolicyConfig;
import org.n52.prosecco.engine.policy.Rule;
import org.n52.prosecco.engine.policy.ValueRestriction;
import org.n52.prosecco.web.ServiceParameters;

public final class RequestContextEvaluatorTest {

    // TODO test with empty parameters --> serviceParameters

    @Test
    public void given_allowingValueRestriction_when_contextWithAllowedValues_then_allowedValuesKept() {
        PolicyConfig config = createSimplePolicyConfig("allow", ValueRestriction.of("phenomenon", "value1", "value2"));
        RequestContextEvaluator evaluator = new RequestContextEvaluator(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withPhenomena("value1", "value2")
                                                           .build();

        FilterContext evaluatedContext = evaluator.evaluate(initialContext);
        assertThat(evaluatedContext.getPhenomena()).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_denyingValueRestriction_when_contextWithAllowedValues_then_deniedValuesRemoved() {
        PolicyConfig config = createSimplePolicyConfig("deny", ValueRestriction.of("phenomenon", "value1", "value2"));
        RequestContextEvaluator evaluator = new RequestContextEvaluator(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withPhenomena("value1", "value2")
                                                           .build();

        FilterContext evaluatedContext = evaluator.evaluate(initialContext);
        assertThat(evaluatedContext.getPhenomena()).doesNotContain("value1", "value2");
    }

    private PolicyConfig createSimplePolicyConfig(String effect, ValueRestriction... valueRestrictions) {
        Policy policy = Policy.of("policy1", effect, valueRestrictions);
        List<Policy> policies = Collections.singletonList(policy);

        List<String> roles = Collections.singletonList("role");
        List<String> policyReferences = Collections.singletonList("policy1");
        Rule rule = new Rule("rule1", roles, policyReferences);
        List<Rule> rules = Collections.singletonList(rule);

        return new PolicyConfig(policies, rules);
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
        RequestContextEvaluator evaluator = new RequestContextEvaluator(config);

        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withPhenomena("value1", "restrictedValue")
                                                           .build();

        FilterContext evaluatedContext = evaluator.evaluate(initialContext);
        assertThat(evaluatedContext.getPhenomena()).containsExactly("value1");
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
        RequestContextEvaluator evaluator = new RequestContextEvaluator(config);

        // simulate an empty request and having cached service parameters 
        ServiceParameters serviceParameters = new ServiceParameters().updatePhenomena("value1", "value2", "restricted");
        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withServiceParameters(serviceParameters)
                                                           .build();

        FilterContext evaluatedContext = evaluator.evaluate(initialContext);
        assertThat(evaluatedContext.getPhenomena()).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_restrictedFloatingTime_when_fullyIncludingBetween_then_timespanBeforeStartRestriction() {
        ValueRestriction timeRestriction = ValueRestriction.of("timespan", "floating,P7D");
        Policy denyingPolicy = Policy.of("denying-policy", timeRestriction);

        List<String> roles = Collections.singletonList("role");
        Rule rule = Rule.of("rule1", roles, "allowing-policy", "denying-policy");

        PolicyConfig config = new PolicyConfig(denyingPolicy, rule);
        RequestContextEvaluator evaluator = new RequestContextEvaluator(config);

        Instant end = Instant.now();
        Instant start = end.minus(4, ChronoUnit.DAYS);
        Timespan queriedTimespan = Timespan.between(start, end);
        FilterContext initialContext = FilterContextBuilder.of("role")
                                                           .withTimespans(queriedTimespan)
                                                           .build();
        
        FilterContext evaluatedContext = evaluator.evaluate(initialContext);
        Optional<Timespan> timespan = evaluatedContext.getFirstTimespan();
        Timespan actual = timespan.get();
        
        assertThat(actual.getRelation()).isEqualTo(TimespanRelation.BEFORE);
        assertThat(Instant.from(actual.getEnd())).satisfies(e -> {
            Instant expectedEnd = end.minus(7, ChronoUnit.DAYS);
            assertThat(e).isBefore(expectedEnd.plus(1, ChronoUnit.SECONDS));
        });
    }
}
