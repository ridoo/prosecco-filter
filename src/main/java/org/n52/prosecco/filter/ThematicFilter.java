
package org.n52.prosecco.filter;

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

final class ThematicFilter implements RequestFilter<String> {

    private final FilterContext context;

    private final PolicyConfig config;

    public ThematicFilter(FilterContext context, PolicyConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public Set<String> evaluate(String parameter, Set<String> values) {
        return evaluateThematic(parameter, values);
    }

    private Set<String> evaluateThematic(String parameter, Set<String> values) {
        return getRelevantRules().stream()
                                 .map(rule -> applyRule(rule, parameter, values))
                                 .flatMap(Set::stream)
                                 .collect(Collectors.toSet());
    }

    private Collection<Rule> getRelevantRules() {
        return config.getRulesForRole(context.getRoles());
    }

    private Set<String> applyRule(Rule rule, String parameter, Set<String> values) {
        List<Policy> allowingPolicies = config.getReferencedPolicies(rule, Effect.ALLOW);
        return values.stream()
                     .filter(configuredValues(parameter, allowingPolicies))
                     .filter(applyPolicies(parameter, allowingPolicies))
                     .collect(Collectors.toSet());
    }

    private Predicate<String> configuredValues(String parameter, List<Policy> allPolicies) {
        return value -> allPolicies.stream()
                                   .map(Policy::getValueRestriction)
                                   .flatMap(Collection::stream)
                                   .filter(matchesRestriction(parameter))
                                   .map(ValueRestriction::getValues)
                                   .flatMap(Collection::stream)
                                   // this applies denied by default
                                   .anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private Predicate<String> applyPolicies(String parameter, List<Policy> policies) {
        return value -> {
            return policies.stream()
                           .map(Policy::getValueRestriction)
                           .flatMap(Collection::stream)
                           .filter(matchesRestriction(parameter))
                           .anyMatch(matchesValue(value));
        };
    }

    private Predicate<ValueRestriction> matchesRestriction(String parameter) {
        return v -> {
            String restrictionName = v.getName();
            return restrictionName.equalsIgnoreCase(parameter);
        };
    }

    private Predicate<ValueRestriction> matchesValue(String value) {
        return restriction -> {
            Set<String> values = restriction.getValues();
            return values.contains(value);
        };

    }
}
