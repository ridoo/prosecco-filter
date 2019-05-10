
package org.n52.prosecco.web.sos.xml;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public final class SosResponseFilterEngine implements ResponseFilterEngine<String> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SosResponseFilterEngine.class);

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
                         Set<String> denied = getAllowedValues(parameter, relevantRules);
                         return xPathsForProcedure.stream()
                                                  .map(xpath -> formatXPath(xpath, denied))
                                                  .flatMap(Collection::stream)
                                                  .collect(Collectors.toSet());
                     })
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());

    }

    private Set<String> getAllowedValues(String parameter, Collection<Rule> relevantRules) {
        Set<String> allowed = extractValues(parameter, relevantRules, Effect.ALLOW);
//        Set<String> denied = extractValues(parameter, relevantRules, Effect.DENY);
//        denied.removeAll(allowed);
        return allowed;
    }

    private Set<String> extractValues(String parameter, Collection<Rule> relevantRules, Effect allow) {
        return relevantRules.stream()
                            .map(rule -> policyConfig.getReferencedPolicies(rule, allow))
                            .flatMap(Collection::stream)
                            .map(Policy::getValueRestriction)
                            .flatMap(Collection::stream)
                            .filter(matchValueRestriction(parameter))
                            .map(ValueRestriction::getValues)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
    }

    private Predicate< ? super ValueRestriction> matchValueRestriction(String parameter) {
        return restriction -> {
            String name = restriction.getName();
            return name.equalsIgnoreCase(parameter);
        };

    }

    private Set<String> formatXPath(String xpath, Set<String> values) {
        return values.stream()
                     .map(value -> formatMessage(xpath, value))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toSet());
    }

    private Optional<String> formatMessage(String xpath, String value) {
        try {
            return Optional.of(MessageFormat.format(xpath, value));
        } catch (Exception e) {
            LOGGER.error("Could not format xpath for value '{}': {}", value, xpath);
            return Optional.empty();
        }
    }

    private Set<String> getXPathsFor(String parameter) {
        Map<String, Set<String>> xpaths = xpathConfig.getCapabilitiesXPathsByParameter();
        return !xpaths.containsKey(parameter)
                ? Collections.emptySet()
                : xpaths.get(parameter);
    }

    private ResponseEntity<String> createEntity(ResponseEntity<String> response, String xml) {
        return new ResponseEntity<>(xml, response.getHeaders(), response.getStatusCode());
    }
}
