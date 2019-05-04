
package org.n52.prosecco.engine.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.prosecco.web.ServiceParameters;

/**
 * Context containing filter parameters of a request.
 */
public final class FilterContext {

    public static class FilterContextBuilder {
        private final Set<String> roles;
        private Map<String, String[]> remainingQuery;
        private ServiceParameters serviceParameters;
        private Set<String> phenomena;
        private Set<String> offerings;
        private Set<String> procedures;
        private Set<String> features;
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

        public FilterContextBuilder andRemainingQuery(Map<String, String[]> remainingQuery) {
            this.remainingQuery = remainingQuery;
            return this;
        }

        public FilterContextBuilder withServiceParameters(ServiceParameters serviceParameters) {
            this.serviceParameters = serviceParameters;
            return this;
        }

        public FilterContextBuilder withPhenomena(String... phenomena) {
            return withPhenomena(toSet(phenomena));
        }

        public FilterContextBuilder withPhenomena(Set<String> phenomena) {
            this.phenomena = phenomena;
            return this;
        }

        public FilterContextBuilder withOfferings(String... offerings) {
            return withOfferings(toSet(offerings));
        }

        public FilterContextBuilder withOfferings(Set<String> offerings) {
            this.offerings = offerings;
            return this;
        }

        public FilterContextBuilder withProcedures(String... procedures) {
            return withProcedures(toSet(procedures));
        }

        public FilterContextBuilder withProcedures(Set<String> procedures) {
            this.procedures = procedures;
            return this;
        }

        public FilterContextBuilder withFeatures(String... features) {
            return withFeatures(toSet(features));
        }

        public FilterContextBuilder withFeatures(Set<String> features) {
            this.features = features;
            return this;
        }

        public FilterContextBuilder withTimespans(Set<Timespan> timespans) {
            this.timespans = timespans;
            return this;
        }

        public FilterContextBuilder withTimespans(Timespan... timespans) {
            Stream<Timespan> items = timespans != null
                    ? Stream.of(timespans).filter(t -> t != null)
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
                                     remainingQuery,
                                     serviceParameters,
                                     phenomena,
                                     offerings,
                                     procedures,
                                     features,
                                     timespans);
        }

    }

    private final Set<String> roles;

    private final Map<String, String[]> remainingQuery;

    private final ServiceParameters serviceParameters;

    private final Set<String> phenomena;

    private final Set<String> offerings;

    private final Set<String> procedures;

    private final Set<String> features;

    private final Set<Timespan> timespans;

    // TODO spatial

    public static FilterContextBuilder of(Set<String> roles) {
        return new FilterContextBuilder(roles);
    }

    private FilterContext(Set<String> roles,
                          Map<String, String[]> remainingQuery,
                          ServiceParameters serviceParameters,
                          Set<String> phenomena,
                          Set<String> offerings,
                          Set<String> procedures,
                          Set<String> features,
                          Set<Timespan> timespans) {
        this.roles = roles;
        this.remainingQuery = remainingQuery;
        this.serviceParameters = serviceParameters;
        this.phenomena = phenomena;
        this.offerings = offerings;
        this.procedures = procedures;
        this.features = features;
        this.timespans = timespans;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Map<String, String[]> getRemainingQuery() {
        return remainingQuery == null
                ? Collections.emptyMap()
                : remainingQuery;
    }

    public ServiceParameters getServiceParameters() {
        return serviceParameters == null
                ? new ServiceParameters()
                : serviceParameters;
    }

    public Set<String> getPhenomena() {
        return hasPhenomena()
                ? Collections.unmodifiableSet(phenomena)
                : Collections.emptySet();
    }

    public boolean hasPhenomena() {
        return hasItems(phenomena);
    }

    public Set<String> getOfferings() {
        return hasOfferings()
                ? Collections.unmodifiableSet(offerings)
                : Collections.emptySet();
    }

    public boolean hasOfferings() {
        return hasItems(offerings);
    }

    public Set<String> getProcedures() {
        return hasProcedures()
                ? Collections.unmodifiableSet(procedures)
                : Collections.emptySet();
    }

    public boolean hasProcedures() {
        return hasItems(procedures);
    }

    public Set<String> getFeatures() {
        return hasFeatures()
                ? Collections.unmodifiableSet(features)
                : Collections.emptySet();
    }

    public boolean hasFeatures() {
        return hasItems(features);
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
