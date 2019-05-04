package org.n52.prosecco.engine.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ValueRestriction {

    private final String name;
    
    private final Set<String> values;
    
    public static ValueRestriction of(String parameterName, String... parameterValues) {
        Set<String> parameters = Stream.of(parameterValues).collect(Collectors.toSet());
        return new ValueRestriction(parameterName, parameters);
    }
    
    public ValueRestriction(@JsonProperty("name") String name, 
                       @JsonProperty("values") Collection<String> values) {
        Objects.requireNonNull(name, "name is null");
        
        this.name = name;
        this.values = values == null
                ? Collections.emptySet()
                : new HashSet<>(values);
    }

    public String getName() {
        return name;
    }

    public Set<String> getValues() {
        return Collections.unmodifiableSet(values);
    }

}
