
package org.n52.prosecco.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class ForwardingRequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardingRequestController.class);

    private final RestTemplate restTemplate;

    private final URI endpoint;

    private final String contextPath;

    private final String pathPrefix;

    protected ForwardingRequestController(URI endpoint, String contextPath, String pathPrefix) {
        Objects.requireNonNull(endpoint, "endpoint is null");
        Objects.requireNonNull(pathPrefix, "pathPrefix is null");

        this.restTemplate = createRestTemplate();

        this.endpoint = endpoint;
        this.pathPrefix = pathPrefix;
        this.contextPath = !"/".equals(contextPath)
            ? contextPath
            : "";
    }

    private RestTemplate createRestTemplate() {
        CloseableHttpClient clientBuilder = HttpClientBuilder.create().build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(clientBuilder));
    }

    protected ResponseEntity< ? > performRequest(URI uri,
                                                 HttpEntity< ? > entity,
                                                 HttpMethod method)
            throws URISyntaxException {
        LOGGER.trace("Forwarding to: {}", uri.toString());
        LOGGER.trace("R E Q U E S T   info:");
        LOGGER.trace("scheme     : {}", uri.getScheme());
        LOGGER.trace("host       : {}", uri.getHost());
        LOGGER.trace("port       : {}", uri.getPort());
        LOGGER.trace("path       : {}", uri.getPath());
        LOGGER.trace("query      : {}", uri.getQuery());
        LOGGER.trace("entity     : {}", entity);
        LOGGER.trace(" ");
        
        ResponseEntity<String> response = restTemplate.exchange(uri, method, entity, String.class);
        
        LOGGER.trace("R E S P O N S E   info:");
        LOGGER.trace("headers         : {}", response.getHeaders());
        LOGGER.trace("StatusCode      : {}", response.getStatusCode());
        LOGGER.trace("body            : {}", response.getBody());
        
        return response;
    }

    protected URI createTargetURI(HttpServletRequest request) throws URISyntaxException {
        String queryString = request.getQueryString();
        return createTargetURI(request, queryString);
    }

    protected URI createTargetURI(HttpServletRequest request, String queryString) throws URISyntaxException {
        String scheme = endpoint.getScheme();
        String host = endpoint.getHost();
        int port = endpoint.getPort();

        String path = createPath(request);
        return new URI(scheme, null, host, port, path, queryString, null);
    }

    private HttpHeaders getHttpHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> sentHeaders = request.getHeaderNames();
        while (sentHeaders.hasMoreElements()) {
            String sentHeader = sentHeaders.nextElement();
            if ( (sentHeader != null) && !sentHeader.toLowerCase().startsWith("origin")) {
                // CORS is not relevant in a non-Javascript context
                httpHeaders.add(sentHeader, request.getHeader(sentHeader));
            }
        }
        return httpHeaders;
    }

    protected HttpEntity< ? > createRequestEntity(HttpServletRequest request) {
        return createRequestEntity(null, request);
    }

    protected HttpEntity< ? > createRequestEntity(String body, HttpServletRequest request) {
        HttpHeaders httpHeader = getHttpHeaders(request);
        return new HttpEntity<>(body, httpHeader);
    }

    private String createPath(HttpServletRequest request) {
        String endpointPath = removeTrailingSlash(endpoint.getPath());
        String targetPath = removePathPrefix(request.getRequestURI());
        return endpointPath + targetPath;
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/")
            ? value.substring(0, value.lastIndexOf("/"))
            : value;
    }

    private String removePathPrefix(String requestURI) {
        return requestURI.substring(contextPath.length() + pathPrefix.length());
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
