
package org.n52.prosecco.engine.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.n52.prosecco.engine.filter.FilterContext;
import org.n52.prosecco.engine.filter.Timespan;
import org.n52.prosecco.engine.filter.TimespanParser;
import org.n52.prosecco.engine.policy.Policy;
import org.n52.prosecco.engine.policy.PolicyConfig;
import org.n52.prosecco.engine.policy.Rule;
import org.n52.prosecco.engine.policy.ValueRestriction;

public final class TimeFloatingEvaluator implements FilterContextEvaluator<Timespan> {

    private final String parameter;

    private final PolicyConfig config;

    public TimeFloatingEvaluator(String parameter, PolicyConfig config) {
        this.parameter = parameter;
        this.config = config;
    }

    @Override
    public Set<Timespan> evaluate(Set<Timespan> values, FilterContext context) {
        return getRelevantRules(context).stream()
                                        .map(rule -> applyPolicies(rule, values))
                                        .flatMap(Set::stream)
                                        .collect(Collectors.toSet());
    }

    private Collection<Rule> getRelevantRules(FilterContext context) {
        return config.getRulesForRole(context.getRoles());
    }

    private Set<Timespan> applyPolicies(Rule rule, Set<Timespan> values) {
        List<Policy> policies = config.getReferencedPolicies(rule);
        return values.stream()
                     .map(value -> applyFilter(value, policies))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    private Set<Timespan> applyFilter(Timespan timespan, List<Policy> policies) {
        return policies.stream()
                       .map(p -> this.alignWithTemporalRestriction(timespan, p))
                       .flatMap(Collection::stream)
                       .collect(Collectors.toSet());

        // for static time filter
        // TODO is within --> split filter
        // TODO overlaps start --> indetermined before restriction start
        // TODO overlaps end --> indetermined after restriction end
        // TODO all allowed may have to be adjusted to match restriction interval
    }

    private Set<Timespan> alignWithTemporalRestriction(Timespan value, Policy policy) {
        List<ValueRestriction> restrictions = policy.getValueRestriction();
        boolean allowed = policy.isAllowed();
        return restrictions.stream()
                           .filter(this::matchesValueRestriction)
                           .map(ValueRestriction::getValues)
                           .map(adjustIfRestricted(value, allowed))
                           .flatMap(Collection::stream)
                           .filter(o -> !o.isEmpty())
                           .map(Optional::get)
                           .collect(Collectors.toSet());
    }


    private boolean matchesValueRestriction(ValueRestriction restriction) {
        String restrictionName = restriction.getName();
        return restrictionName.equalsIgnoreCase(parameter);
    }

    private Function<Set<String>, Set<Optional<Timespan>>> adjustIfRestricted(Timespan value, boolean allowed) {
        return restrictedValues -> {
            
            if (restrictedValues.size() > 1) {

                // TODO check outputs (open ends) against further restrictions
                // 
                // ---------------[  query  ]----------------------------->
                // ------------[  restricted1  ]-------------------------->
                // (---split1--]               [--split2-----------------)> after 1st recursion
                // ---------------------------------[  restricted2  ]-----> split2 to be splitted
                //                                
                
                throw new IllegalStateException("Multiple floating restrictions not supported right now!");
            }

            Optional<String> firstRestriction = restrictedValues.stream().findFirst();
            if (firstRestriction.isEmpty()) {
                return Collections.emptySet();
            }
            
            TimespanParser parser = new TimespanParser();
            Timespan restriction = parser.parseTimeRestriction(firstRestriction.get());
            
            if (restriction.isOverlapping(value) && !allowed) {
                return wrapAsSet(Timespan.before(restriction.getStart()));
            }
            
            return wrapAsSet(value);
        };
    }

    private Set<Optional<Timespan>> wrapAsSet(Timespan alignedValue) {
        return Collections.singleton(Optional.of(alignedValue));
    }
    
}
