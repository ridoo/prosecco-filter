package org.n52.prosecco.policy;

import java.util.Optional;

public enum Effect {
    
    DENY("deny"), ALLOW("allow");
    
    private final String effect;
    
    private Effect(String effect) {
        this.effect = effect;
    }
    
    public String getEffect() {
        return effect;
    }
    
    public static boolean isEffect(String effect) {
        return findEffect(effect).isPresent();
    }

    public static Effect toEffect(String effect) {
        return findEffect(effect).orElseThrow(() -> new IllegalArgumentException("Invalid effect: " + effect));
    }

    private static Optional<Effect> findEffect(String effect) {
        for (Effect value : values()) {
            if (value.getEffect().equalsIgnoreCase(effect)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
} 
