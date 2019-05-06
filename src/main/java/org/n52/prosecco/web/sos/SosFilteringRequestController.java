
package org.n52.prosecco.web.sos;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

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
public final class SosFilteringRequestController extends ForwardingRequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosFilteringRequestController.class);

    private static final String PATH_PREFIX = "/sos";

    private final SosFilterRequestService requestService;
    
    private final SosFilterResponseService responseService;

    SosFilteringRequestController(@Value("${prosecco.target.url}") URI endpoint,
                                  @Value("${prosecco.servlet.context-path}") String contextPath,
                                  SosFilterRequestService requestService,
                                  SosFilterResponseService responseService) {
        super(endpoint, contextPath, PATH_PREFIX);
        this.requestService = requestService;
        this.responseService = responseService;
        // this.parameterCache = performCacheUpdate();
    }

    @ResponseBody
    @RequestMapping(value = PATH_PREFIX + "/**", method = GET)
    public ResponseEntity< ? > filterGet(HttpServletRequest request, HttpMethod method) throws URISyntaxException {
        LOGGER.debug("Filter GET request on: {}", PATH_PREFIX);
        try {
            LOGGER.trace("O R I G I N A L   query: {}", request.getQueryString());
            String queryString = requestService.filterGET(request);
            HttpEntity< ? > entity = createRequestEntity(request);
            URI uri = createTargetURI(request, queryString);
            
            ResponseEntity< ? > response = performRequest(uri, entity, method);
            return responseService.filter(response);
        } catch (FilterRequestException e) {
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
//                String body = requestService.readRequestBody(request);
//                LOGGER.trace("O R I G I N A L   entity: {}", body);
            }
            String body = requestService.filterPOST(request);
            URI uri = createTargetURI(request);
            HttpEntity< ? > entity = createRequestEntity(body, request);
            
            ResponseEntity< ? > response = performRequest(uri, entity, method);
            return responseService.filter(response);
        } catch (FilterRequestException e) {
            LOGGER.debug("Could not filter request!", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Throwable e) {
            LOGGER.debug("Error processing the request!", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
