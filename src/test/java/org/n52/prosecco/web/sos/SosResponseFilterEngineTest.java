package org.n52.prosecco.web.sos;

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
import org.xmlunit.assertj.XmlAssert;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        Policy policy = Policy.of("policy1", "deny", ValueRestriction.of("procedure", "file32"));
        PolicyConfig policyConfig = new PolicyConfig(policy, Rule.of("rule1", "role", "policy1"));
        
        AuthenticationContext authContext = createStaticAuthContext("role");
        SosResponseFilterEngine engine = new SosResponseFilterEngine(policyConfig, xPathConfig, authContext);

        String xmlBeforeFiltering = capabilitiesResponse.getBody();
        String xpath = ""
                + "/*[local-name()='Capabilities']"
                + "/*" // OperationsMetadata
                + "/*" // Operation
                + "/*[local-name()='Parameter' and @name='procedure']"
                + "/*" // AllowedValues
                + "/*[local-name()='Value' and text()='file32']"
                ;

        XmlAssert.assertThat(xmlBeforeFiltering).hasXPath(xpath);
        ResponseEntity<String> filtered = engine.filter(capabilitiesResponse);
        XmlAssert.assertThat(filtered.getBody()).doesNotHaveXPath(xpath);
        
        // TODO check offerings
    }

    private AuthenticationContext createStaticAuthContext(String ... roles) {
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
