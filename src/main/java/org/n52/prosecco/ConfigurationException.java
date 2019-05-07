package org.n52.prosecco;

public class ConfigurationException extends Exception {

    private static final long serialVersionUID = -7271268152977362891L;

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
