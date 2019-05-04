
package org.n52.prosecco.web;

import static java.net.URI.create;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class FilteringRequestControllerTest {

    @Test
    public void given_contextPath_when_requestEndpoint_then_backendUrlIsCorrect() throws Exception {
        URI endpoint = create("https://endpoint.org/sos");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/prosecco-webapp/sos/service");
        ForwardingRequestController controller = new ForwardingRequestControllerSeam(endpoint, "/prosecco-webapp", "/sos");
        URI createPath = controller.createTargetURI(request);
        assertThat(createPath.getPath(), is("/sos/service"));
    }

    @Test
    public void given_contextPathIsSlash_when_requestEndpoint_then_backendUrlIsCorrect() throws Exception {
        URI endpoint = create("https://endpoint.org/sos");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sos/service");
        ForwardingRequestController controller = new ForwardingRequestControllerSeam(endpoint, "/", "/sos");
        URI createPath = controller.createTargetURI(request);
        assertThat(createPath.getPath(), is("/sos/service"));
    }
    
    private static class ForwardingRequestControllerSeam extends ForwardingRequestController {
        protected ForwardingRequestControllerSeam(URI endpoint, String contextPath, String pathPrefix) {
            super(endpoint, contextPath, pathPrefix);
        }
    }

}
