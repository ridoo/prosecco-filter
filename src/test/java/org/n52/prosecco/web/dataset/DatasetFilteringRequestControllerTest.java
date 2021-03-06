
package org.n52.prosecco.web.dataset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.n52.prosecco.AuthenticationContext;
import org.n52.prosecco.AuthenticationContext.AuthenticationContextBuilder;
import org.n52.prosecco.ConfigurationContainer;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.policy.PolicyConfig;
import org.n52.prosecco.policy.ValueRestriction;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class DatasetFilteringRequestControllerTest {

    private static final String SERVER_ENDPOINT = "http://somewhere.net/api";

    @Test
    public void given_policyWithOfferings_when_queryWithOfferings_then_allowedOfferingsAreDelegated() throws Exception {
        ValueRestriction restriction = ValueRestriction.of("offering", "off1", "off2");
        PolicyConfig config = PolicyConfig.createSimple("allow", "role", restriction);
        ControllerSeam seam = ControllerSeam.of(config, Collections.singleton("role"));
        
        MockRestServiceServer server = seam.getServer();
        server.expect(method(HttpMethod.GET))
              .andExpect(queryParam("offerings", "off1,off2"))
              .andRespond(withSuccess());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ds");
        request.addParameter("offerings", "off1", "off2", "restricted");
        seam.filterGet(request, HttpMethod.GET);

        server.verify();
    }
    
    @Test
    public void given_emptyOfferings_when_queryOnlyRestrictedValues_then_exception() throws Exception {
        ControllerSeam seam = ControllerSeam.of(new PolicyConfig(), Collections.singleton("role"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ds");
        request.addParameter("offerings", "off1");
        ResponseEntity<String> response = seam.filterGet(request, HttpMethod.GET);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    private static class ControllerSeam extends DatasetFilteringRequestController {

        static ControllerSeam of(PolicyConfig policyConfig, Set<String> roles) throws URISyntaxException {
            String contextPath = "/";
            URI endpoint = new URI(SERVER_ENDPOINT);
            AuthenticationContext authContext = AuthenticationContextBuilder.withRoles(roles);
            ConfigurationContainer config = new ConfigurationContainer("ds", policyConfig);
            RequestFilterEngine engine = new RequestFilterEngine(config);
            
            DatasetFilterRequestService requestService = new DatasetFilterRequestService(engine, authContext);
            DatasetFilterResponseService responseService = new DatasetFilterResponseService();
            return new ControllerSeam(endpoint, contextPath, requestService, responseService);
        }

        private final MockRestServiceServer server;

        private ControllerSeam(URI endpoint,
                               String contextPath,
                               DatasetFilterRequestService requestService,
                               DatasetFilterResponseService responseService) {
            super(endpoint, contextPath, requestService, responseService);
            this.server = createServer(this);
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return super.getRestTemplate();
        }

        private MockRestServiceServer createServer(ControllerSeam seam) {
            RestTemplate restTemplate = seam.getRestTemplate();
            return MockRestServiceServer.bindTo(restTemplate)
                                        .build();
        }

        MockRestServiceServer getServer() {
            return server;
        }

    }
}
