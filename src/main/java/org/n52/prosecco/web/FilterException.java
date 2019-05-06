package org.n52.prosecco.web;

public class FilterException extends Exception {

    private static final long serialVersionUID = 3556248523782792609L;

    public FilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterException(String message) {
        super(message);
    }

}
