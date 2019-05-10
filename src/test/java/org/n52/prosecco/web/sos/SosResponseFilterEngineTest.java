
package org.n52.prosecco.web.sos;

import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.sos.xml.SosResponseFilterEngine;
import org.n52.prosecco.web.sos.xml.XPathConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.MessageFormat;

public class SosResponseFilterEngineTest {

    private XPathConfig xPathConfig;

    private ResponseEntity<String> capabilitiesResponse;

    @Before
    public void setUp() throws Exception {
        File config = loadFileContent("/xpath-expressions.json");
        xPathConfig = new ObjectMapper().readValue(config, XPathConfig.class);

        File capabilities = loadFileContent("/capabilities.xml");
        String content = Files.readString(capabilities.toPath());
        capabilitiesResponse = new ResponseEntity<>(content, HttpStatus.OK);
    }

    @Test
    public void test() throws Exception {
        Policy policy = Policy.of("policy1", "allow", ValueRestriction.of("procedure", "file32"));
        PolicyConfig policyConfig = new PolicyConfig(policy, Rule.of("rule1", "role", "policy1"));

        String allowedProceduresXPath = ""
                + "/*[local-name()='Capabilities']"
                + "/*" // OperationsMetadata
                + "/*" // Operation
                + "/*[local-name()='Parameter' and @name='procedure']"
                + "/*" // AllowedValues
                + "/*[local-name()='Value' and {0}]";
        
        String availableOfferingsXPath = ""
                + "/*[local-name()='Capabilities']"
                + "/*" // contents
                + "/*" // Contents
                + "/*" // offering
                + "/*[local-name()=\"ObservationOffering\" and *[local-name()=\"procedure\" and text()=\"{0}\"]]";

        String xmlBeforeFiltering = capabilitiesResponse.getBody();
        assertThat(xmlBeforeFiltering).hasXPath(MessageFormat.format(allowedProceduresXPath, "text()='homer'"))
                                      .withFailMessage("Expected state is already satisfied before, but the test did not run yet! "
                                              + "This will yield into wrong expectations!");

        AuthenticationContext authContext = createStaticAuthContext("role");
        SosResponseFilterEngine engine = new SosResponseFilterEngine(policyConfig, xPathConfig, authContext);
        ResponseEntity<String> filtered = engine.filter(capabilitiesResponse);

        assertThat(filtered.getBody()).satisfies(xml -> {
            assertThat(xml).doesNotHaveXPath(MessageFormat.format(allowedProceduresXPath, "text()='homer'"));
            assertThat(xml).doesNotHaveXPath(MessageFormat.format(availableOfferingsXPath, "text()='homer'"));
            assertThat(xml).hasXPath(MessageFormat.format(allowedProceduresXPath, "text()='file32'"));
        });
    }

    private AuthenticationContext createStaticAuthContext(String... roles) {
        return new AuthenticationContext() {
            @Override
            public Set<String> getRoles() {
                return new HashSet<>(Arrays.asList(roles));
            }
        };
    }

    private File loadFileContent(String name) throws URISyntaxException {
        URL resource = getClass().getResource(name);
        return new File(resource.toURI());
    }

}
