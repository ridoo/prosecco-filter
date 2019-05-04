
package org.n52.prosecco.web.sos;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.n52.prosecco.engine.policy.Policy;
import org.n52.prosecco.engine.policy.PolicyConfig;
import org.n52.prosecco.engine.policy.RequestContextEvaluator;
import org.n52.prosecco.engine.policy.Rule;
import org.n52.prosecco.engine.policy.ValueRestriction;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class SosFilterGetRequestServiceTest {

    @Test
    public void given_requestWithNonFilterableParameters_when_filterRequest_then_nonFilterableParametersRemain()
            throws FilterRequestException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("other", "x,y");
        request.addParameter("other", "z");

        RequestContextEvaluator evaluator = new RequestContextEvaluator(new PolicyConfig());
        SosFilterRequestService service = new SosFilterGetRequestService(evaluator);
        String queryString = service.filter(request);

        assertThat(queryString.split("&")).containsExactly("other=x,y,z");
    }

    @Test
    public void given_policyConfig_when_requestGetCapabilities_then_noFilteringTakesPlace()
            throws FilterRequestException {
        ValueRestriction valueRestriction = ValueRestriction.of("phenomenon", "allow", "value1");
        List<Policy> policies = Arrays.asList(Policy.of("policy1", valueRestriction));
        PolicyConfig policyConfig = new PolicyConfig(policies, Rule.of("foo1", "role", "policy1"));

        RequestContextEvaluator evaluator = new RequestContextEvaluator(policyConfig);
        SosFilterRequestService service = new SosFilterGetRequestService(evaluator);
        MockHttpServletRequest request = createServletRequest("GetCapabilities");
        String queryString = service.filter(request);

        assertThat(queryString).contains("GetCapabilities").doesNotContain("phenomonon");
    }

    private MockHttpServletRequest createServletRequest(String operation) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if (!"GetCapabilities".equalsIgnoreCase(operation)) {
            servletRequest.addParameter("service", "SOS");
        }
        servletRequest.addParameter("version", "2.0.0");
        servletRequest.addParameter("request", operation);
        return servletRequest;
    }
}
