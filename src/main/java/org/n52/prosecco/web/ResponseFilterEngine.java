package org.n52.prosecco.web;

import org.n52.prosecco.AuthenticationContext;
import org.springframework.http.ResponseEntity;

public interface ResponseFilterEngine<T> {

    ResponseEntity<T> filter(ResponseEntity<T> entity) throws FilterException;
}
