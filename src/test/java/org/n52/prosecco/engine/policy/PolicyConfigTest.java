
package org.n52.prosecco.engine.policy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyConfigTest {

    @Test
    public void when_ruleReferencesKnownPolicy_when_getViaPoliciesViaRule_then_allPoliciesFound() throws IOException,
            PolicyConfigException {
        List<Policy> policies = Arrays.asList(Policy.of("policy1"),
                                              Policy.of("policy2"),
                                              Policy.of("policy3"));
        Rule rule = Rule.of("foo", "role", "policy1", "policy2");
        PolicyConfig policyConfig = new PolicyConfig(policies, rule);
        assertThat(policyConfig.getReferencedPolicies(rule)).anyMatch(p -> "policy1".equals(p.getName()))
                                                            .anyMatch(p -> "policy2".equals(p.getName()))
                                                            .noneMatch(p -> "policy3".equals(p.getName()));
    }

    @Test
    public void when_roleReferencedInMultipleRules_when_getRulesViaRole_then_allRulesFound() throws IOException,
            PolicyConfigException {
        List<Rule> rules = Arrays.asList(Rule.of("foo1", "role", "policy1", "policy2"),
                                         Rule.of("foo2", "role"),
                                         Rule.of("foo3", "other-role"));
        PolicyConfig policyConfig = new PolicyConfig(Collections.emptyList(), rules);
        assertThat(policyConfig.getRulesForRole("role")).anyMatch(p -> "foo1".equals(p.getName()))
                                                        .anyMatch(p -> "foo2".equals(p.getName()))
                                                        .noneMatch(p -> "foo3".equals(p.getName()));
    }
}
