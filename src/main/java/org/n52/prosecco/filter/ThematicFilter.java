
package org.n52.prosecco.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.n52.prosecco.policy.Effect;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.request.FilterContext;

final class ThematicFilter implements FilterContextFilter<String> {

    private final String parameter;
    private final PolicyConfig config;

    public ThematicFilter(String parameter, PolicyConfig config) {
        this.parameter = parameter;
        this.config = config;
    }

    @Override
    public Set<String> evaluate(Set<String> values, FilterContext context) {
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
        List<Policy> allPolicies = new ArrayList<>();
        allPolicies.addAll(allowingPolicies);
        allPolicies.addAll(denyingPolicies);
        return values.stream()
                     .filter(deniedByDefault(allPolicies))
                     .filter(policyConfiguration(allowingPolicies, denyingPolicies))
                     .collect(Collectors.toSet());
    }

    private Predicate< ? super String> deniedByDefault(List<Policy> allPolicies) {
        return value -> allPolicies.stream()
                                   .map(Policy::getValueRestriction)
                                   .flatMap(Collection::stream)
                                   .map(ValueRestriction::getValues)
                                   .flatMap(Collection::stream)
                                   .anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private Predicate< ? super String> policyConfiguration(List<Policy> allowingPolicies,
                                                           List<Policy> denyingPolicies) {
        return value -> {
            boolean allowed = matchesThematicRestriction(value, allowingPolicies);
            boolean denied = !matchesThematicRestriction(value, denyingPolicies);
            return allowed || denied;
        };
    }

    private boolean matchesThematicRestriction(String value, List<Policy> policies) {
        return policies.stream()
                       .map(Policy::getValueRestriction)
                       .flatMap(Collection::stream)
                       .filter(this::matchesValueRestriction)
                       .anyMatch(matchesValue(value));
    }

    private boolean matchesValueRestriction(ValueRestriction restriction) {
        String restrictionName = restriction.getName();
        return restrictionName.equalsIgnoreCase(parameter);
    }

    private Predicate< ? super ValueRestriction> matchesValue(String value) {
        return valueRestriction -> valueRestriction.getValues()
                                                   .contains(value);
    }
}
