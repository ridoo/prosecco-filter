package org.n52.prosecco.policy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Rule {

    private final String name;
    private final List<String> roles;
    private final List<String> policies;
    
    public static Rule of(String name, String role, String... policies) {
        return new Rule(name, Collections.singletonList(role), Stream.of(policies).collect(Collectors.toList()));
    }
    
    public static Rule of(String name, List<String> roles, String... policies) {
        return new Rule(name, roles, Stream.of(policies).collect(Collectors.toList()));
    }
    
    public Rule(@JsonProperty("name") String name, 
                @JsonProperty("roles") List<String> roles, 
                @JsonProperty("policies") List<String> policies) {
        Objects.requireNonNull(name, "name is null");
        
        this.name = name;
        this.roles = roles == null 
                ? Collections.emptyList()
                : roles;
        this.policies = policies == null
                ? Collections.emptyList()
                : policies;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPolicies() {
        return policies;
    }
    
}
