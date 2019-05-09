
package org.n52.prosecco.web.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.prosecco.web.ServiceParameters;

/**
 * Context containing filter parameters of a request.
 */
public final class FilterContext {

    public static class FilterContextBuilder {
        private final Set<String> roles;
        private Map<String, Set<String>> parameters;
        private Map<String, Set<String>> remainingQuery;
        private ServiceParameters serviceParameters;
        private Set<Timespan> timespans;

        public FilterContextBuilder(Set<String> roles) {
            this.roles = roles;
        }

        public static FilterContextBuilder of(String role) {
            return of(Collections.singleton(role));
        }

        public static FilterContextBuilder of(Set<String> roles) {
            return new FilterContextBuilder(roles);
        }

        /**
         * Sets query parameters which remain after applying the given {@code predicate}.
         * 
         * @param query
         *        the query
         * @param predicate
         *        a predicate to filter the remaining parameters
         * @return the builder instance for method chaining
         */
        public FilterContextBuilder andRemainingFrom(Map<String, String[]> query,
                                                     Predicate<Entry<String, String[]>> predicate) {
            this.remainingQuery = query.entrySet()
                                       .stream()
                                       .filter(predicate)
                                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                                 entry -> new HashSet<>(Arrays.asList(entry.getValue()))));
            return this;
        }

        public FilterContextBuilder andRemainingFrom(FilterContext context) {
            this.remainingQuery = context.getRemainingQuery();
            return this;
        }
        
        public FilterContextBuilder withServiceParameters(FilterContext context) {
            return withServiceParameters(context.getServiceParameters());
        }

        public FilterContextBuilder withServiceParameters(ServiceParameters serviceParameters) {
            this.serviceParameters = serviceParameters;
            return this;
        }

        public FilterContextBuilder withParameters(String parameter, String... values) {
            return withParameters(parameter, toSet(values));
        }

        public FilterContextBuilder withParameters(String parameter, Set<String> values) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(parameter, values);
            return this;
        }

        public FilterContextBuilder withTimespans(Set<Timespan> timespans) {
            this.timespans = timespans;
            return this;
        }

        public FilterContextBuilder withTimespans(Timespan... timespans) {
            Stream<Timespan> items = timespans != null
                    ? Stream.of(timespans)
                            .filter(t -> t != null)
                    : Stream.empty();
            this.timespans = toSet(items);
            return this;
        }

        private Set<String> toSet(String... items) {
            return items == null
                    ? Collections.emptySet()
                    : toSet(Stream.of(items)
                                  .map(i -> i.split(","))
                                  .map(Arrays::asList)
                                  .flatMap(Collection::stream));
        }

        private <T> Set<T> toSet(Stream<T> items) {
            return items != null
                    ? items.collect(Collectors.toSet())
                    : Collections.emptySet();
        }

        public FilterContext build() {
            return new FilterContext(roles,
                                     parameters,
                                     remainingQuery,
                                     serviceParameters,
                                     timespans);
        }

    }

    private final Set<String> roles;

    private final Map<String, Set<String>> parameters;

    private final Map<String, Set<String>> remainingQuery;

    private final ServiceParameters serviceParameters;

    private final Set<Timespan> timespans;

    // TODO spatial

    public static FilterContext empty() {
        return of(Collections.emptySet()).build();
    }

    public static FilterContextBuilder of(Set<String> roles) {
        return new FilterContextBuilder(roles);
    }

    private FilterContext(Set<String> roles,
                          Map<String, Set<String>> parameters,
                          Map<String, Set<String>> remainingQuery,
                          ServiceParameters serviceParameters,
                          Set<Timespan> timespans) {
        this.roles = roles;
        this.parameters = parameters;
        this.remainingQuery = remainingQuery;
        this.serviceParameters = serviceParameters;
        this.timespans = timespans;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Map<String, Set<String>> getRemainingQuery() {
        return remainingQuery == null
                ? Collections.emptyMap()
                : remainingQuery;
    }

    public ServiceParameters getServiceParameters() {
        return serviceParameters == null
                ? new ServiceParameters()
                : serviceParameters;
    }

    public Set<String> getServiceParameterValues(String parameter) {
        ServiceParameters parameters = getServiceParameters();
        return parameters.getValues(parameter);
    }

    public Set<String> getValues(String parameter) {
        return hasParameter(parameter)
                ? Collections.unmodifiableSet(parameters.get(parameter))
                : Collections.emptySet();
    }

    public boolean hasParameter(String parameter) {
        return parameters != null && hasItems(parameters.get(parameter));
    }

    public Set<String> getThematicParameterNames() {
        ServiceParameters serviceParameterNames = getServiceParameters();
        Set<String> serviceParameters = serviceParameterNames.getThematicParameterNames();
        Set<String> queryParameters = parameters != null
                ? Collections.unmodifiableSet(parameters.keySet())
                : Collections.emptySet();
        Set<String> availableParameters = new HashSet<>();
        availableParameters.addAll(serviceParameters);
        availableParameters.addAll(queryParameters);
        return availableParameters;
    }

    public Set<Timespan> getTimespans() {
        return hasTimespans()
                ? Collections.unmodifiableSet(timespans)
                : Collections.emptySet();
    }

    private boolean hasTimespans() {
        return hasItems(timespans);
    }

    public Optional<Timespan> getFirstTimespan() {
        return timespans.stream()
                        .findFirst();
    }

    private boolean hasItems(Collection< ? > items) {
        return items != null && !items.isEmpty();
    }

}
