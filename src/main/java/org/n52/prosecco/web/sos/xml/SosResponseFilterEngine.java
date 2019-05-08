
package org.n52.prosecco.web.sos.xml;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.policy.Effect;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.ResponseFilterEngine;
import org.springframework.http.ResponseEntity;

public final class SosResponseFilterEngine implements ResponseFilterEngine<String> {

    private final PolicyConfig policyConfig;

    private final XPathConfig xpathConfig;

    private final AuthenticationContext authContext;

    public SosResponseFilterEngine(PolicyConfig policyConfig,
                                   XPathConfig xpathConfig,
                                   AuthenticationContext authContext) {
        this.policyConfig = policyConfig;
        this.xpathConfig = xpathConfig;
        this.authContext = authContext;
    }

    @Override
    public ResponseEntity<String> filter(ResponseEntity<String> response) throws FilterException {
        XmlDocumentFilter filter = new XmlDocumentFilter(response);
        Set<String> xpaths = getXPathFilterExpressions(filter.getDocumentName());
        filter.applyRemove(xpaths);

        return createEntity(response, filter.getFilteredXml());
    }

    private Set<String> getXPathFilterExpressions(String documentName) {
        Collection<Rule> relevantRules = policyConfig.getRulesForRole(authContext.getRoles());

        // TODO filter other operation responses than capabilities

        return Stream.of("procedure", "phenomenon", "offering", "feature")
                     .map(parameter -> {
                         Set<String> xPathsForProcedure = getXPathsFor(parameter);
                         Set<String> denied = getDeniedValues(parameter, relevantRules);
                         return xPathsForProcedure.stream()
                                                  .map(xpath -> formatXPath(xpath, denied))
                                                  .flatMap(Collection::stream)
                                                  .collect(Collectors.toSet());
                     })
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());

    }

    private Set<String> getDeniedValues(String parameter, Collection<Rule> relevantRules) {
        Set<String> allowed = relevantRules.stream()
                                           .map(rule -> policyConfig.getReferencedPolicies(rule, Effect.ALLOW))
                                           .flatMap(Collection::stream)
                                           .map(Policy::getValueRestriction)
                                           .flatMap(Collection::stream)
                                           .filter(r -> r.getName()
                                                         .equalsIgnoreCase(parameter))
                                           .map(ValueRestriction::getValues)
                                           .flatMap(Collection::stream)
                                           .collect(Collectors.toSet());

        Set<String> denied = relevantRules.stream()
                                          .map(rule -> policyConfig.getReferencedPolicies(rule, Effect.DENY))
                                          .flatMap(Collection::stream)
                                          .map(Policy::getValueRestriction)
                                          .flatMap(Collection::stream)
                                          .filter(r -> r.getName()
                                                        .equalsIgnoreCase(parameter))
                                          .map(ValueRestriction::getValues)
                                          .flatMap(Collection::stream)
                                          .collect(Collectors.toSet());
        denied.removeAll(allowed);
        return denied;
    }

    private Set<String> formatXPath(String xpath, Set<String> denied) {
        return denied.stream()
                     // TODO to test
                     .map(deniedValue -> MessageFormat.format(xpath, deniedValue))
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
