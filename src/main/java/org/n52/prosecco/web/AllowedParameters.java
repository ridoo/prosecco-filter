
package org.n52.prosecco.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds values which are allowed. The contents may differ depending on the policy configuration and the role
 * the requesting user belongs to. Therefore an instance is volatile but may be cached for each user/role.
 */
public final class AllowedParameters {

    private Map<String, Set<String>> parameters;

    public AllowedParameters() {
        this.parameters = new HashMap<>();
    }

    public AllowedParameters(String parameter, String... values) {
        this();
        updateParameter(parameter, values);
    }

    public AllowedParameters updateParameter(String parameter, String... values) {
        parameters.put(parameter, asSet(values));
        return this;
    }

    private Set<String> asSet(String... items) {
        Stream<String> stream = Stream.of(items);
        return stream.collect(Collectors.toSet());
    }

    public Set<String> getParameterNames() {
        return Collections.unmodifiableSet(parameters.keySet());
    }

    public Set<String> getValues(String parameter) {
        Set<String> values = parameters.get(parameter);
        return hasItems(values)
                ? Collections.unmodifiableSet(values)
                : Collections.emptySet();
    }

    private boolean hasItems(Set<String> values) {
        return values != null && !values.isEmpty();
    }

}
