package org.n52.prosecco.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Policy {
    
    private final String name;
    private final Effect effect;
    private final List<ValueRestriction> valueRestriction;
    
    public static Policy of(String name, ValueRestriction... valueRestriction) {
        return Policy.of(name, null, valueRestriction);
    }
    
    public static Policy of(String name, String effect, ValueRestriction... valueRestriction) {
        return new Policy(name, effect, Stream.of(valueRestriction).collect(Collectors.toList()));
    }
    
    @JsonCreator
    public Policy(@JsonProperty("name") String name, 
                  @JsonProperty("effect") String effect, 
                  @JsonProperty("valueRestriction") Collection<ValueRestriction> valueRestriction) {
        Objects.requireNonNull(name, "name is null");
        
        this.name = name;
        
        this.effect = effect != null ? Effect.toEffect(effect) : Effect.DENY;
        this.valueRestriction = valueRestriction == null
                ? Collections.emptyList()
                : new ArrayList<>(valueRestriction);
    }

    public String getName() {
        return name;
    }

    Effect getEffect() {
        return effect;
    }
    
    public boolean isAllowed() {
        return effect == Effect.ALLOW;
    }
    
    public boolean isDenied() {
        return effect == Effect.DENY;
    }

    public List<ValueRestriction> getValueRestriction() {
        return Collections.unmodifiableList(valueRestriction);
    }

}
