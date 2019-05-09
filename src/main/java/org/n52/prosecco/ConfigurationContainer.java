package org.n52.prosecco;

import java.util.HashMap;
import java.util.Map;

import org.n52.prosecco.policy.PolicyConfig;

public final class ConfigurationContainer {

    private final Map<String, PolicyConfig> policyConfigs;

    public static ConfigurationContainer create(String endpoint, PolicyConfig config) {
        return new ConfigurationContainer().addConfig(endpoint, config);
    }
    
    private ConfigurationContainer() {
        this.policyConfigs = new HashMap<>();
    }
    
    public ConfigurationContainer addConfig(String endpoint, PolicyConfig config) {
        this.policyConfigs.put(endpoint, config);
        return this;
    }
    
    public PolicyConfig getConfig(String endpoint) {
        return policyConfigs.get(endpoint);
    }
}
