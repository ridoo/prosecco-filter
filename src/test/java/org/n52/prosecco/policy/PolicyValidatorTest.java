package org.n52.prosecco.policy;

import java.io.IOException;

import org.junit.Test;
import org.n52.prosecco.ConfigurationException;

public class PolicyValidatorTest {

    @Test(expected = ConfigurationException.class)
    public void when_ruleReferencesUnknownPolicy_when_validatingConfig_then_exception() throws IOException, ConfigurationException {
        Policy policy = Policy.of("known");
        Rule rule = Rule.of("foo", "role", "unknown");
        PolicyConfig policyConfig = new PolicyConfig(policy, rule);
        new PolicyValidator(policyConfig).validate();
    }

}
