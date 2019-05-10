
package org.n52.prosecco.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.request.FilterContext;
import org.n52.prosecco.web.request.Timespan;
import org.n52.prosecco.web.request.TimespanParser;

final class TimeFloatingFilter implements RequestFilter<Timespan> {

    private final FilterContext context;

    private final PolicyConfig config;

    public TimeFloatingFilter(FilterContext context, PolicyConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public Set<Timespan> evaluate(String parameter, Set<Timespan> values) {
        return getRelevantRules().stream()
                                 .map(rule -> applyPolicies(rule, parameter, values))
                                 .flatMap(Set::stream)
                                 .collect(Collectors.toSet());
    }

    private Collection<Rule> getRelevantRules() {
        return config.getRulesForRole(context.getRoles());
    }

    private Set<Timespan> applyPolicies(Rule rule, String parameter, Set<Timespan> values) {
        List<Policy> policies = config.getReferencedPolicies(rule);
        return values.stream()
                     .map(value -> applyFilter(parameter, value, policies))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    private Set<Timespan> applyFilter(String parameter, Timespan timespan, List<Policy> policies) {
        return policies.stream()
                       .map(applyFilter(parameter, timespan))
                       .flatMap(Collection::stream)
                       .collect(Collectors.toSet());

        // for static time filter
        // TODO is within --> split filter
        // TODO overlaps start --> indetermined before restriction start
        // TODO overlaps end --> indetermined after restriction end
        // TODO all allowed may have to be adjusted to match restriction interval
    }

    private Function<Policy, Set<Timespan>> applyFilter(String parameter, Timespan timespan) {
        return policy -> {
            List<ValueRestriction> restrictions = policy.getValueRestriction();
            boolean allowed = policy.isAllowed();
            return restrictions.stream()
                               .filter(matchesValueRestriction(parameter))
                               .map(ValueRestriction::getValues)
                               .map(adjustIfRestricted(timespan, allowed))
                               .flatMap(Collection::stream)
                               .filter(o -> !o.isEmpty())
                               .map(Optional::get)
                               .collect(Collectors.toSet());
        };
    }

    private Predicate< ? super ValueRestriction> matchesValueRestriction(String parameter) {
        return rrestriction -> {
            String restrictionName = rrestriction.getName();
            return restrictionName.equalsIgnoreCase(parameter);
        };
    }

    private Function<Set<String>, Set<Optional<Timespan>>> adjustIfRestricted(Timespan value, boolean allowed) {
        return restrictedValues -> {

            if (restrictedValues.size() > 1) {

                // TODO check outputs (open ends) against further restrictions
                //
                // ---------------[ query ]----------------------------->
                // ------------[ restricted1 ]-------------------------->
                // (---split1--] [--split2-----------------)> after 1st recursion
                // ---------------------------------[ restricted2 ]-----> split2 to be splitted
                //

                throw new IllegalStateException("Multiple floating restrictions not supported right now!");
            }

            Optional<String> firstRestriction = restrictedValues.stream()
                                                                .findFirst();
            if (firstRestriction.isEmpty()) {
                return Collections.emptySet();
            }

            TimespanParser parser = new TimespanParser();
            Timespan restriction = parser.parseTimeRestriction(firstRestriction.get());

            if (restriction.isOverlapping(value) && !allowed) {
                return wrapAsSet(Timespan.before(restriction.getStart()));
            }
            
            // TODO allowed restriction does not overlap value --> split

            return wrapAsSet(value);
        };
    }

    private Set<Optional<Timespan>> wrapAsSet(Timespan alignedValue) {
        return Collections.singleton(Optional.of(alignedValue));
    }

}
