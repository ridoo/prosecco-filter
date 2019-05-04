package org.n52.prosecco.engine.policy;

import java.io.IOException;

import org.junit.Test;

public class PolicyValidatorTest {

    @Test(expected = PolicyConfigException.class)
    public void when_ruleReferencesUnknownPolicy_when_validatingConfig_then_exception() throws IOException, PolicyConfigException {
        Policy policy = Policy.of("known");
        Rule rule = Rule.of("foo", "role", "unknown");
        PolicyConfig policyConfig = new PolicyConfig(policy, rule);
        new PolicyValidator(policyConfig).validate();
    }

}
