
package org.n52.prosecco.web.dataset;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.DroppedQueryConditionException;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.ForwardingRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class DatasetFilteringRequestController extends ForwardingRequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetFilteringRequestController.class);

    private static final String PATH_PREFIX = "/ds";

    private final DatasetFilterRequestService requestService;

    private final DatasetFilterResponseService responseService;

    DatasetFilteringRequestController(@Value("${prosecco.dataset.url}") URI endpoint,
                                      @Value("${prosecco.servlet.context-path:/}") String contextPath,
                                      DatasetFilterRequestService requestService,
                                      DatasetFilterResponseService responseService) {
        super(endpoint, contextPath, PATH_PREFIX);
        this.requestService = requestService;
        this.responseService = responseService;
    }

    @ResponseBody
    @RequestMapping(value = PATH_PREFIX + "/**", method = GET)
    public ResponseEntity<String> filterGet(HttpServletRequest request, HttpMethod method) throws URISyntaxException {
        LOGGER.debug("Filter GET request on: {}", PATH_PREFIX);
        try {
            LOGGER.trace("O R I G I N A L   query: {}", request.getQueryString());
            String queryString = requestService.filterGET(request);
            HttpEntity<String> entity = createRequestEntity(request);
            URI uri = createTargetURI(request, queryString);

            ResponseEntity<String> response = performRequest(uri, entity, method);
            return responseService.filter(response);
        } catch (DroppedQueryConditionException e) {
            LOGGER.info("Returning an 'empty' response.", e);
            return new ResponseEntity<>("[]", HttpStatus.OK);
        } catch (FilterException e) {
            LOGGER.debug("Could not filter request!", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Throwable e) {
            LOGGER.debug("Error processing the request!", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(value = PATH_PREFIX + "/**", method = POST)
    public ResponseEntity< ? > filterPost(HttpServletRequest request, HttpMethod method) throws URISyntaxException {
        LOGGER.debug("Filter POST request on: {}", PATH_PREFIX);
        try {
            if (LOGGER.isTraceEnabled()) {
                String body = readRequestBody(request);
                LOGGER.trace("O R I G I N A L   entity: {}", body);
            }
            String body = requestService.filterPOST(request);
            HttpEntity<String> entity = createRequestEntity(body, request);
            URI uri = createTargetURI(request);

            ResponseEntity<String> response = performRequest(uri, entity, method);
            return responseService.filter(response);
        } catch (FilterException e) {
            LOGGER.debug("Could not filter request!", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Throwable e) {
            LOGGER.debug("Error processing the request!", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines()
                         .collect(Collectors.joining());
        }
    }

}
