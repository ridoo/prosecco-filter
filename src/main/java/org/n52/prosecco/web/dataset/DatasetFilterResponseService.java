package org.n52.prosecco.web.dataset;

import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.FilterResponseService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DatasetFilterResponseService implements FilterResponseService<String> {

    @Override
    public ResponseEntity<String> filter(ResponseEntity<String> response) throws FilterException {
        // TODO Auto-generated method stub
        return null;
    }

}
