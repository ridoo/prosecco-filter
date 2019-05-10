
package org.n52.prosecco;

import java.io.IOException;
import java.util.Optional;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.PolicyValidator;
import org.n52.prosecco.web.sos.xml.SosResponseFilterEngine;
import org.n52.prosecco.web.sos.xml.XPathConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class ProseccoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProseccoApplication.class, args);
    }

    @Bean
    public AuthenticationContext getAuthenticationContext() {
        return new SpringSecurityAuthenticationContext();
    }

    @Bean
    public ConfigurationContainer getPolicyConfig(@Value("${prosecco.sos.policy}") Resource sosConfigFile,
                                                  @Value("${prosecco.dataset.policy}") Resource dsConfigFile)
            throws IOException, ConfigurationException {
        ConfigurationContainer configurationContainer = new ConfigurationContainer();
        addConfiguration("ds", dsConfigFile, configurationContainer);
        addConfiguration("sos", sosConfigFile, configurationContainer);
        return configurationContainer;
    }

    @Bean
    public RequestFilterEngine getRequestFilterEngine(ConfigurationContainer configuration)
            throws IOException, ConfigurationException {
        return new RequestFilterEngine(configuration);
    }

    @Bean
    public SosResponseFilterEngine getResponseFilterEngine(AuthenticationContext authenticationContext,
                                                           @Value("${prosecco.sos.xpaths}") Resource xpathConfigFile,
                                                           ConfigurationContainer configurationContainer)
            throws IOException, ConfigurationException {
        PolicyConfig policyConfig = configurationContainer.getConfig("sos");
        XPathConfig xpathConfig = readConfig(xpathConfigFile, XPathConfig.class);
        return new SosResponseFilterEngine(policyConfig, xpathConfig, authenticationContext);
    }

    private void addConfiguration(String endpoint, Resource resource, ConfigurationContainer container)
            throws IOException, ConfigurationException {
        readPolicyConfig(resource).ifPresent(c -> container.addConfig(endpoint, c));
    }

    private Optional<PolicyConfig> readPolicyConfig(Resource configFile) throws IOException, ConfigurationException {
        if (configFile == null) {
            return Optional.empty();
        } else {
            PolicyConfig config = readConfig(configFile, PolicyConfig.class);
            return Optional.of(new PolicyValidator(config).validate());
        }
    }

    private <T> T readConfig(Resource configFile, Class<T> clazz) throws IOException, ConfigurationException {
        JsonFileReader configReader = new JsonFileReader(configFile.getFile());
        return configReader.readConfig(clazz);
    }

}
