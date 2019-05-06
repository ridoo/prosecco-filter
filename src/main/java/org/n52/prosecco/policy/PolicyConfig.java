
package org.n52.prosecco.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PolicyConfig {

    private final List<Policy> policies;
    private final List<Rule> rules;
    
    public PolicyConfig() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public PolicyConfig(Policy policy, Rule rule) {
        this(Collections.singletonList(policy), Collections.singletonList(rule));
    }

    public PolicyConfig(List<Policy> policies, Rule rule) {
        this(policies, Collections.singletonList(rule));
    }

    public PolicyConfig(List<Policy> policies, Rule... rules) {
        this(policies, Stream.of(rules).collect(Collectors.toList()));
    }

    @JsonCreator
    public PolicyConfig(@JsonProperty("policies") List<Policy> policies,
                        @JsonProperty("rules") List<Rule> rules) {
        this.policies = policies == null
            ? Collections.emptyList()
            : policies;
        this.rules = rules == null
            ? Collections.emptyList()
            : rules;
    }

    public List<Policy> getPolicies() {
        return Collections.unmodifiableList(policies);
    }

    public boolean hasPolicies() {
        return ! (this.policies == null || this.policies.isEmpty());
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public boolean hasRules() {
        return ! (this.rules == null || this.rules.isEmpty());
    }
    
    public Collection<Rule> getRulesForRole(String role) {
        Set<String> roles = Collections.singleton(role);
        return rules.stream().filter(containsAny(roles)).collect(Collectors.toList());
    }

    public Collection<Rule> getRulesForRole(Set<String> roles) {
        return rules.stream().filter(containsAny(roles)).collect(Collectors.toList());
    }
    
    private Predicate<Rule> containsAny(Set<String> roles) {
        return rule -> {
            Set<String> configuredRoles = new HashSet<>(rule.getRoles());
            configuredRoles.retainAll(roles);
            return !configuredRoles.isEmpty();
        };
    }

    /**
     * Finds all policies referenced by the given rule.
     * 
     * @param rule
     *        the rule
     * @return the policies found referenced by the given rule
     */
    public List<Policy> getReferencedPolicies(Rule rule) {
        return getReferencedPolicies(rule, null);
    }

    /**
     * Finds all policies referenced by the given rule and matching given effect (may be {@code null}).
     * 
     * @param rule
     *        the rule
     * @param effect
     *        the effect to match ({@code null} matches any)
     * @return the policies found referenced by the given rule and effect
     */
    public List<Policy> getReferencedPolicies(Rule rule, Effect effect) {
        List<String> referencedPolicies = rule.getPolicies();
        return policies.stream()
                       .filter(p -> referencedPolicies.contains(p.getName()))
                       .filter(p -> effect == null || p.getEffect() == effect)
                       .collect(Collectors.toList());
    }
}
