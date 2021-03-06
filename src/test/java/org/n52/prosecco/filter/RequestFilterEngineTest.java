
package org.n52.prosecco.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.n52.prosecco.ConfigurationContainer;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.AllowedParameters;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanRelation;

public final class RequestFilterEngineTest {

    // TODO test with empty parameters --> allowedParameters

    @Test
    public void given_allowingValueRestriction_when_contextWithAllowedValues_then_allowedValuesKept()
            throws DroppedQueryConditionException {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "value1", "value2");
        PolicyConfig policyConfig = PolicyConfig.createSimple("allow", "role", restriction);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContext.create("sos", "role")
                                                    .withParameters("phenomenon", "value1", "value2")
                                                    .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_simpleConfig_when_contextWithHasUnconfiguredValues_then_unconfiguredValuesRemoved()
            throws DroppedQueryConditionException {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "value1");
        PolicyConfig policyConfig = PolicyConfig.createSimple("allow", "role", restriction);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContext.create("sos", "role")
                                                    .withParameters("phenomenon", "value1", "value2")
                                                    .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsExactly("value1");
    }

    @Test
    public void given_phenomenaFilter_when_contextWithAllowedAndDeniedValues_then_allowedValuesKeptOnly()
            throws DroppedQueryConditionException {
        ValueRestriction valueRestriction1 = ValueRestriction.of("phenomenon", "value1", "value2");
        ValueRestriction valueRestriction2 = ValueRestriction.of("phenomenon", "restrictedValue");
        List<Policy> policies = Arrays.asList(Policy.of("allowing", "allow", valueRestriction1),
                                              Policy.of("denying", valueRestriction2));

        Rule rule = Rule.of("rule1", "role", "allowing", "denying");
        PolicyConfig policyConfig = new PolicyConfig(policies, rule);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContext.create("sos", "role")
                                                    .withParameters("phenomenon", "value1", "restrictedValue")
                                                    .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsExactly("value1");
    }

    @Test
    public void given_allowedParameters_when_contextIsEmpty_then_allowedValuesAddedOnly()
            throws DroppedQueryConditionException {
        ValueRestriction valueRestriction1 = ValueRestriction.of("phenomenon", "value1", "value2");
        ValueRestriction valueRestriction2 = ValueRestriction.of("phenomenon", "restricted");
        Policy allowingPolicy = Policy.of("allowing", "allow", valueRestriction1);
        Policy denyingPolicy = Policy.of("denying", valueRestriction2);
        List<Policy> policies = Arrays.asList(allowingPolicy, denyingPolicy);

        Rule rule = Rule.of("rule1", "role", "allowing", "denying");
        PolicyConfig policyConfig = new PolicyConfig(policies, rule);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        // simulate an empty request and having cached service parameters
        AllowedParameters allowedParameters = new AllowedParameters("phenomenon", "value1", "value2", "restricted");
        FilterContext initialContext = FilterContext.create("sos", "role")
                                                    .withAllowedParameters(allowedParameters)
                                                    .build();

        FilterContext evaluatedContext = engine.evaluate(initialContext);
        assertThat(evaluatedContext.getValues("phenomenon")).containsAll(Arrays.asList("value1", "value2"));
    }

    @Test
    public void given_restrictedFloatingTime_when_fullyIncludingBetween_then_timespanBeforeStartRestriction()
            throws DroppedQueryConditionException {
        ValueRestriction timeRestriction = ValueRestriction.of("timespan", "floating,P7D");
        Policy denyingPolicy = Policy.of("denying", timeRestriction);
        Rule rule = Rule.of("rule1", "role", "allowing", "denying");

        PolicyConfig policyConfig = new PolicyConfig(denyingPolicy, rule);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        Instant end = Instant.now();
        Instant start = end.minus(4, ChronoUnit.DAYS);
        Timespan queriedTimespan = Timespan.between(start, end);
        FilterContext initialContext = FilterContext.create("sos", "role")
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

    @Test(expected = DroppedQueryConditionException.class)
    public void given_restrictedValues_when_allQueryValuesFiltered_then_exception()
            throws DroppedQueryConditionException {
        ValueRestriction timeRestriction = ValueRestriction.of("offering", "value1");
        Policy denyingPolicy = Policy.of("denying", timeRestriction);
        Rule rule = Rule.of("rule1", "role", "denying");

        PolicyConfig policyConfig = new PolicyConfig(denyingPolicy, rule);
        ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
        RequestFilterEngine engine = new RequestFilterEngine(config);

        FilterContext initialContext = FilterContext.create("sos", "role")
                                                    .withParameters("offering", "value1")
                                                    .build();

        engine.evaluate(initialContext);
    }
}
