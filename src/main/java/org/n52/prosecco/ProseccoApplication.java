
package org.n52.prosecco;

import java.io.IOException;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.policy.PolicyValidator;
import org.n52.prosecco.policy.PolicyConfig;
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
    public PolicyConfig getPolicyConfig(@Value("${prosecco.config.policy}") Resource configFile)
            throws ConfigurationException, IOException {
        ConfigReader configReader = new ConfigReader(configFile.getFile());
        PolicyConfig config = configReader.readConfig(PolicyConfig.class);
        return new PolicyValidator(config).validate();
    }

    @Bean
    public RequestFilterEngine getRequestFilterEngine(PolicyConfig policyConfig) {
        return new RequestFilterEngine(policyConfig);
    }
    }

}
