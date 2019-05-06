
package org.n52.prosecco;

import java.io.IOException;

import org.n52.prosecco.engine.eval.RequestContextEvaluator;
import org.n52.prosecco.engine.policy.PolicyConfigException;
import org.n52.prosecco.engine.policy.PolicyReader;
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
    public RequestContextEvaluator getPolicyReader(@Value("${prosecco.config.policy}") Resource configFile) throws PolicyConfigException, IOException {
        PolicyReader policyReader = new PolicyReader(configFile.getFile());
        return new RequestContextEvaluator(policyReader.readConfig());
    }

}

