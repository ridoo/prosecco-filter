
package org.n52.prosecco.web.sos.xml;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.ResponseFilterEngine;
import org.springframework.http.ResponseEntity;

public final class SosResponseFilterEngine implements ResponseFilterEngine<String> {

    private final PolicyConfig policyConfig;

    private final XPathConfig xpathConfig;

    public SosResponseFilterEngine(PolicyConfig policyConfig, XPathConfig xpathConfig) {
        this.policyConfig = policyConfig;
        this.xpathConfig = xpathConfig;
    }

    @Override
    public ResponseEntity<String> filter(ResponseEntity<String> response) throws FilterException {
        XmlDocumentFilter filter = new XmlDocumentFilter(response);
        Set<String> xpaths = getFilterExpressions(filter.getDocumentName());
        filter.applyRemove(xpaths);

        return createEntity(response, filter.getFilteredXml());
    }

    private Set<String> getFilterExpressions(String documentName) {
        Set<String> xpaths = new HashSet<>();
        Collection<Rule> relevantRules = policyConfig.getRulesForRole(getRoles());

        // List<Policy> allowingPolicies = config.getReferencedPolicies(rule, Effect.ALLOW);
        // List<Policy> denyingPolicies = config.getReferencedPolicies(rule, Effect.DENY);

        // TODO Auto-generated method stub

        Set<String> xPathsForProcedure = getXPathsFor("procedure");
        return xPathsForProcedure.stream()
                 // TODO 
                                 .map(xpath -> MessageFormat.format(xpath, "file32"))
                                 .collect(Collectors.toSet());
    }

    private Set<String> getXPathsFor(String string) {
        Map<String, Set<String>> xpaths = xpathConfig.getCapabilitiesXPathsByParameter();
        return !xpaths.containsKey(string)
                ? Collections.emptySet()
                : xpaths.get(string);
    }

    private ResponseEntity<String> createEntity(ResponseEntity<String> response, String xml) {
        return new ResponseEntity<>(xml, response.getHeaders(), response.getStatusCode());
    }
}
