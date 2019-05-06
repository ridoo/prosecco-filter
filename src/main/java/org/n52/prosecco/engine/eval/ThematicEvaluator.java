
package org.n52.prosecco.engine.eval;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.n52.prosecco.engine.filter.FilterContext;
import org.n52.prosecco.engine.policy.Effect;
import org.n52.prosecco.engine.policy.Policy;
import org.n52.prosecco.engine.policy.PolicyConfig;
import org.n52.prosecco.engine.policy.Rule;
import org.n52.prosecco.engine.policy.ValueRestriction;

public final class ThematicEvaluator implements FilterContextEvaluator<String> {

    private final String parameter;
    private final PolicyConfig config;

    public ThematicEvaluator(String parameter, PolicyConfig config) {
        this.parameter = parameter;
        this.config = config;
    }

    @Override
    public Set<String> evaluate(Set<String> values, FilterContext context) {
        // case empty query: apply rule on all available values
        return evaluateThematic(values, context);
    }

    private Set<String> evaluateThematic(Set<String> values, FilterContext context) {
        return getRelevantRules(context).stream()
                                        .map(rule -> applyPolicies(rule, values))
                                        .flatMap(Set::stream)
                                        .collect(Collectors.toSet());
    }

    private Collection<Rule> getRelevantRules(FilterContext context) {
        return config.getRulesForRole(context.getRoles());
    }

    private Set<String> applyPolicies(Rule rule, Set<String> values) {
        List<Policy> allowingPolicies = config.getReferencedPolicies(rule, Effect.ALLOW);
        List<Policy> denyingPolicies = config.getReferencedPolicies(rule, Effect.DENY);
        return values.stream()
                     .filter(matchesPolicy(allowingPolicies, denyingPolicies))
                     .collect(Collectors.toSet());
    }

    private Predicate< ? super String> matchesPolicy(List<Policy> allowingPolicies, List<Policy> denyingPolicies) {
        return value -> isAllowed(value, allowingPolicies)
                || !isDenied(value, denyingPolicies);
    }

    private boolean isAllowed(String value, List<Policy> allowingPolicies) {
        return matchesThematicRestriction(value, allowingPolicies);
    }

    private boolean isDenied(String value, List<Policy> denyingPolicies) {
        // TODO value not explicitly denied will be allowed
        return matchesThematicRestriction(value, denyingPolicies);
    }

    private boolean matchesThematicRestriction(String value, List<Policy> policies) {
        return policies.stream()
                       .map(policy -> policy.getValueRestriction())
                       .flatMap(Collection::stream)
                       .filter(this::matchesValueRestriction)
                       .anyMatch(matchesValue(value));
    }

    private boolean matchesValueRestriction(ValueRestriction restriction) {
        return restriction.getName()
                          .equalsIgnoreCase(parameter);
    }

    private Predicate< ? super ValueRestriction> matchesValue(String value) {
        return valueRestriction -> valueRestriction.getValues()
                                                   .contains(value);
    }
}
