package org.n52.prosecco.engine.policy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PolicyValidator {
    
    private final PolicyConfig policyConfig;

    public PolicyValidator(PolicyConfig config) {
        this.policyConfig = config;
    }

    public PolicyConfig validate() throws PolicyConfigException {
        if (this.policyConfig == null) {
            throw new PolicyConfigException("no policy to validate!");
        }
        
        validatePolicyReferences(policyConfig);
        
        // TODO add further validation rules
        
        return policyConfig;
    }

    private void validatePolicyReferences(PolicyConfig config) throws PolicyConfigException {
        List<Policy> policies = config.getPolicies();
        Set<String> policyNames = policies.stream()
                                          .map(Policy::getName)
                                          .collect(Collectors.toSet());
        
        List<Rule> rules = config.getRules();
        for (Rule rule : rules) {
            if ( !policyNames.containsAll(rule.getPolicies())) {
                throw new PolicyConfigException("Rule '" + rule.getName() + "' contains unknown policy references!");
            }
        }
    }

}
