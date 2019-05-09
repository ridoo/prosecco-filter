
package org.n52.prosecco.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ServiceParameters {

    private Map<String, Set<String>> parameters;

    public ServiceParameters() {
        this.parameters = new HashMap<>();
    }
    
    public ServiceParameters(String parameter, String... values) {
        this();
        updateParameter(parameter, values);
    }

    public ServiceParameters updateParameter(String parameter, String... values) {
        parameters.put(parameter, asSet(values));
        return this;
    }

    private Set<String> asSet(String... items) {
        Stream<String> stream = Stream.of(items);
        return stream.collect(Collectors.toSet());
    }
    
    public Set<String> getThematicParameterNames() {
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
