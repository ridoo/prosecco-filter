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
            return filterEngine.filter(response);
        } catch (FilterException e) {
            LOGGER.error("Returning unfiltered response!", e);
            return response;
        }
    }

}
