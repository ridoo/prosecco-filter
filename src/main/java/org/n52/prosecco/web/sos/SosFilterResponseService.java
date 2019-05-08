package org.n52.prosecco.web.sos;

import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.sos.xml.SosResponseFilterEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterResponseService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SosFilterResponseService.class);
    
    private final SosResponseFilterEngine filterEngine;

    public SosFilterResponseService(SosResponseFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public ResponseEntity<String> filter(ResponseEntity<String> response) {
        try {
            ResponseEntity<String> filteredResponse = filterEngine.filter(response);
            
            LOGGER.trace("F I L T E R E D   R E S P O N S E   info:");
            LOGGER.trace("headers         : {}", response.getHeaders());
            LOGGER.trace("StatusCode      : {}", response.getStatusCode());
            LOGGER.trace("body            : {}", response.getBody());
            
            return filteredResponse;
        } catch (FilterException e) {
            LOGGER.error("Returning unfiltered response!", e);
            return response;
        }
    }

}
