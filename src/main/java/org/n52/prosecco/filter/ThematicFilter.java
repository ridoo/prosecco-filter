
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
        List<Policy> denyingPolicies = config.getReferencedPolicies(rule, Effect.DENY);
        List<Policy> allPolicies = new ArrayList<>();
        allPolicies.addAll(allowingPolicies);
        allPolicies.addAll(denyingPolicies);
        return values.stream()
                     .filter(notConfiguredAtAll(allPolicies))
                     .filter(applyPolicies(parameter, allowingPolicies, denyingPolicies))
                     .collect(Collectors.toSet());
    }

    private Predicate< ? super String> notConfiguredAtAll(List<Policy> allPolicies) {
        return value -> allPolicies.stream()
                                   .map(Policy::getValueRestriction)
                                   .flatMap(Collection::stream)
                                   .map(ValueRestriction::getValues)
                                   .flatMap(Collection::stream)
                                   // this applies denied by default
                                   .anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private Predicate< ? super String> applyPolicies(String parameter,
                                                     List<Policy> allowingPolicies,
                                                     List<Policy> denyingPolicies) {
        return value -> {
            boolean allowed = matchesThematicRestriction(parameter, value, allowingPolicies);
            boolean denied = !matchesThematicRestriction(parameter, value, denyingPolicies);
            return allowed || denied;
        };
    }

    private boolean matchesThematicRestriction(String parameter, String value, List<Policy> policies) {
        return policies.stream()
                       .map(Policy::getValueRestriction)
                       .flatMap(Collection::stream)
                       .filter(v -> matchesValueRestriction(parameter, v))
                       .anyMatch(matchesValue(value));
    }

    private boolean matchesValueRestriction(String parameter, ValueRestriction restriction) {
        String restrictionName = restriction.getName();
        return restrictionName.equalsIgnoreCase(parameter);
    }

    private Predicate< ? super ValueRestriction> matchesValue(String value) {
        return valueRestriction -> valueRestriction.getValues()
                                                   .contains(value);
    }
}
