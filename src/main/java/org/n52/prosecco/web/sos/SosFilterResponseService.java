package org.n52.prosecco.web.sos;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public final class SosFilterResponseService {

    public ResponseEntity< ? > filter(ResponseEntity< ? > response) {
        
        // TODO perform Capabilities filtering
        
        return response;
    }

}
