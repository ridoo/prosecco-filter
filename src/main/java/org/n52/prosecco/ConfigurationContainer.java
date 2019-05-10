package org.n52.prosecco;

import java.util.HashMap;
import java.util.Map;

import org.n52.prosecco.policy.PolicyConfig;

public final class ConfigurationContainer {

    private final Map<String, PolicyConfig> policyConfigs;

    public ConfigurationContainer() {
        this(null, null);
    }

    public ConfigurationContainer(String endpoint, PolicyConfig config) {
        this.policyConfigs = new HashMap<>();
        addConfig(endpoint, config);
    }
    
    public void addConfig(String endpoint, PolicyConfig config) {
        if (endpoint != null && config != null) {
            this.policyConfigs.put(endpoint, config);
        }
    }
    
    public PolicyConfig getConfig(String endpoint) {
        return policyConfigs.get(endpoint);
    }
}
