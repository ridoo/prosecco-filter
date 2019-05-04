
package org.n52.prosecco.engine.policy;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class PolicyReader {

    private final File policyConfig;

    public PolicyReader(File policyConfig) {
        Objects.requireNonNull(policyConfig, "policy file is null");
        if (! (policyConfig.exists() && policyConfig.canRead())) {
            String path = policyConfig.getAbsolutePath();
            throw new IllegalArgumentException("Can not access policy file at " + path + " (does it exists?)");
        }
        this.policyConfig = policyConfig;
    }

    public PolicyConfig readConfig() throws PolicyConfigException {
        try {
            ObjectMapper om = new ObjectMapper();
            PolicyConfig config = om.readValue(policyConfig, PolicyConfig.class);
            return new PolicyValidator(config).validate();
        } catch (IOException e) {
            String path = policyConfig.getAbsolutePath();
            throw new PolicyConfigException("Can not read policy from configuration file: " + path, e);
        }
    }

}
