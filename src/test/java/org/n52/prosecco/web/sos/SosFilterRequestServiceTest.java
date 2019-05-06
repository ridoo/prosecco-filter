
package org.n52.prosecco.web.sos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.n52.prosecco.filter.RequestContextFilter;
import org.n52.prosecco.policy.Policy;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.Rule;
import org.n52.prosecco.policy.ValueRestriction;
import org.n52.prosecco.web.FilterException;
import org.springframework.mock.web.MockHttpServletRequest;

public class SosFilterRequestServiceTest {

    @Test
    public void given_requestWithNonFilterableParameters_when_filterRequest_then_nonFilterableParametersRemain()
            throws FilterException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("other", "x,y");
        request.addParameter("other", "z");

        RequestContextFilter evaluator = new RequestContextFilter(new PolicyConfig());
        SosFilterRequestService service = new SosFilterRequestService(evaluator);
        String queryString = service.filterGET(request);

        assertThat(queryString.split("&")).containsExactly("other=x,y,z");
    }

    @Test
    public void given_policyConfig_when_requestGetCapabilities_then_noFilteringTakesPlace()
            throws FilterException {
        ValueRestriction valueRestriction = ValueRestriction.of("phenomenon", "allow", "value1");
        List<Policy> policies = Arrays.asList(Policy.of("policy1", valueRestriction));
        PolicyConfig policyConfig = new PolicyConfig(policies, Rule.of("foo1", "role", "policy1"));

        RequestContextFilter evaluator = new RequestContextFilter(policyConfig);
        SosFilterRequestService service = new SosFilterRequestService(evaluator);
        MockHttpServletRequest request = createServletRequest("GetCapabilities");
        String queryString = service.filterGET(request);

        assertThat(queryString).contains("GetCapabilities")
                               .doesNotContain("phenomonon");
    }

    private MockHttpServletRequest createServletRequest(String operation) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if ( !"GetCapabilities".equalsIgnoreCase(operation)) {
            servletRequest.addParameter("service", "SOS");
        }
        servletRequest.addParameter("version", "2.0.0");
        servletRequest.addParameter("request", operation);
        return servletRequest;
    }
}
