package org.n52.prosecco.web;

import org.springframework.http.ResponseEntity;

public interface FilterResponseService<T> {

    ResponseEntity<T> filter(ResponseEntity<T> response) throws FilterException;
}
