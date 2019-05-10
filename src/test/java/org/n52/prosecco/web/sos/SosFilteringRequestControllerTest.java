
package org.n52.prosecco.web.sos;

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
import org.n52.prosecco.web.sos.xml.SosResponseFilterEngine;
import org.n52.prosecco.web.sos.xml.XPathConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.xmlunit.assertj.XmlAssert;

public class SosFilteringRequestControllerTest {

    private static final String SERVER_ENDPOINT = "http://somewhere.net/sos";

    @Test
    public void given_policyWithOfferings_when_queryWithOfferings_then_allowedOfferingsAreDelegated() throws Exception {
        ValueRestriction restriction = ValueRestriction.of("phenomenon", "p1", "p2");
        PolicyConfig config = PolicyConfig.createSimple("allow", "role", restriction);
        ControllerSeam seam = ControllerSeam.of(config, null, "role");

        MockRestServiceServer server = seam.getServer();
        server.expect(method(HttpMethod.GET))
              .andExpect(queryParam("observedProperty", "p1,p2"))
              .andRespond(withSuccess());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sos");
        request.addParameter("observedProperty", "p1", "p2", "restricted");
        seam.filterGet(request, HttpMethod.GET);

        server.verify();
    }

    @Test
    public void given_emptyOfferings_when_queryOnlyRestrictedValues_then_exception() throws Exception {
        ControllerSeam seam = ControllerSeam.of(new PolicyConfig(), null, "role");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sos");
        request.addParameter("observedProperty", "p1");
        ResponseEntity<String> response = seam.filterGet(request, HttpMethod.GET);
        XmlAssert.assertThat(response.getBody())
                 .withNamespaceContext(Collections.singletonMap("ows", "http://www.opengis.net/ows/1.1"))
                 .hasXPath("//ows:ExceptionReport")
                 .exist();
    }

    private static class ControllerSeam extends SosFilteringRequestController {

        static ControllerSeam of(PolicyConfig policyConfig, XPathConfig xpathConfig, String role)
                throws URISyntaxException {
            return of(policyConfig, xpathConfig, Collections.singleton(role));
        }

        static ControllerSeam of(PolicyConfig policyConfig, XPathConfig xpathConfig, Set<String> roles)
                throws URISyntaxException {
            String contextPath = "/";
            URI endpoint = new URI(SERVER_ENDPOINT);
            AuthenticationContext authContext = AuthenticationContextBuilder.withRoles(roles);
            ConfigurationContainer config = new ConfigurationContainer("sos", policyConfig);
            RequestFilterEngine requestFilterEngine = new RequestFilterEngine(config);
            SosResponseFilterEngine responseFilterEngine = new SosResponseFilterEngine(policyConfig,
                                                                                       xpathConfig,
                                                                                       authContext);

            SosFilterRequestService requestService = new SosFilterRequestService(requestFilterEngine, authContext);
            SosFilterResponseService responseService = new SosFilterResponseService(responseFilterEngine);
            return new ControllerSeam(endpoint, contextPath, requestService, responseService);
        }

        private final MockRestServiceServer server;

        private ControllerSeam(URI endpoint,
                               String contextPath,
                               SosFilterRequestService requestService,
                               SosFilterResponseService responseService) {
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
