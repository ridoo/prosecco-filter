
package org.n52.prosecco.web;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ServiceParameters {
    
    private Set<String> phenomena;

    private Set<String> offerings;

    private Set<String> procedures;

    private Set<String> features;
    
    public ServiceParameters() {
        this.phenomena = new HashSet<>();
        this.offerings = new HashSet<>();
        this.procedures = new HashSet<>();
        this.features = new HashSet<>();
    }
    
    public ServiceParameters updatePhenomena(String... phenomena) {
        this.phenomena = asSet(phenomena);
        return this;
    }
    
    public ServiceParameters updateIfferings(String... offerings) {
        this.offerings = asSet(offerings);
        return this;
    }
    
    public ServiceParameters updateProcedures(String... procedures) {
        this.procedures = asSet(procedures);
        return this;
    }

    public ServiceParameters updateFeatures(String... features) {
        this.features = asSet(features);
        return this;
    }
    
    private Set<String> asSet(String... items) {
        return Stream.of(items).collect(Collectors.toSet());
    }

    public Set<String> getPhenomena() {
        return new HashSet<>(phenomena);
    }

    public Set<String> getOfferings() {
        return new HashSet<>(offerings);
    }

    public Set<String> getProcedures() {
        return new HashSet<>(procedures);
    }

    public Set<String> getFeatures() {
        return new HashSet<>(features);
    }
    
}
