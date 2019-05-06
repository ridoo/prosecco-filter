
package org.n52.prosecco.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.n52.prosecco.policy.Effect;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.PolicyConfigException;
import org.n52.prosecco.policy.PolicyReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PolicyReaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ObjectMapper om = new ObjectMapper();

    @Test(expected = NullPointerException.class)
    public void exceptionWhenNullConfigFile() {
        new PolicyReader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionWhenMissingConfigFile() {
        File configFile = new File("does not exist");
        new PolicyReader(configFile);
    }

    @Test(expected = PolicyConfigException.class)
    public void exceptionWhenInvalidConfigFile() throws IOException, PolicyConfigException {
        File configFile = writeTempFile("not json content");
        new PolicyReader(configFile).readConfig();
    }

    @Test
    public void emptyPoliciesWhenEmptyConfigFile() throws IOException, PolicyConfigException {
        ObjectNode root = om.createObjectNode();
        File configFile = writeTempFile(root);
        PolicyConfig config = new PolicyReader(configFile).readConfig();
        assertThat(config.hasPolicies()).isFalse();
    }

    @Test
    public void emptyPoliciesWhenEmptyPoliciesNode() throws IOException, PolicyConfigException {
        ObjectNode root = om.createObjectNode();
        root.putArray("policies");

        File configFile = writeTempFile(root);
        PolicyConfig config = new PolicyReader(configFile).readConfig();
        assertThat(config.hasPolicies()).isFalse();
    }

    @Test
    public void parsingSinglePolicyUsingDefaultEffect() throws IOException, PolicyConfigException {
        ObjectNode root = om.createObjectNode();
        ArrayNode policies = root.putArray("policies");
        ObjectNode expected = policies.addObject()
                                      .put("name", "policy1");

        File configFile = writeTempFile(root);
        PolicyConfig config = new PolicyReader(configFile).readConfig();
        assertThat(config.getPolicies()).hasSize(1);

        assertThat(config.getPolicies().get(0)).satisfies(p -> {
            assertThat(p.getName()).isEqualTo(expected.get("name").asText());
            assertThat(p.getEffect()).isEqualTo(Effect.DENY);
            assertThat(p.getValueRestriction()).hasSize(0);
        });
    }

    @Test
    public void emptyRulesWhenEmptyRulesNode() throws IOException, PolicyConfigException {
        ObjectNode root = om.createObjectNode();
        root.putArray("rules");

        File configFile = writeTempFile(root);
        PolicyConfig config = new PolicyReader(configFile).readConfig();
        assertThat(config.hasRules()).isFalse();
    }

    private File writeTempFile(ObjectNode root) throws IOException {
        return writeTempFile(root.toString());
    }

    private File writeTempFile(String content) throws IOException {
        File configFile = tempFolder.newFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(content);
        }
        return configFile;
    }
}