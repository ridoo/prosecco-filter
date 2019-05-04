
package org.n52.prosecco.web.sos;

import static java.net.URI.create;

import java.net.URI;

import org.junit.Test;

public class SosFilteringRequestControllerTest {

    @Test
    public void given_contextPath_when_requestEndpoint_then_backendUrlIsCorrect() throws Exception {
        URI endpoint = create("https://endpoint.org/sos");

//        MockMvcRequestBuilders.post("/prosecco-webapp/sos/service")
//                              .content("invalid")
//                              .bui
//        
//        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", endpoint);
//        servletRequest.
//        SosFilteringRequestController controller = new SosFilteringRequestController(endpoint, "/prosecco-webapp");
//        
        // TODO filter
        
    }

}
