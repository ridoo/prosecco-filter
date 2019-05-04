package org.n52.prosecco.web.sos;

public class FilterRequestException extends Exception {

    private static final long serialVersionUID = 3556248523782792609L;

    public FilterRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterRequestException(String message) {
        super(message);
    }

}
