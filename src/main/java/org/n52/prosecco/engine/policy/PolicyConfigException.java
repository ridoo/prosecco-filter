package org.n52.prosecco.engine.policy;

public class PolicyConfigException extends Exception {

    private static final long serialVersionUID = -7271268152977362891L;

    public PolicyConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyConfigException(String message) {
        super(message);
    }

    public PolicyConfigException(Throwable cause) {
        super(cause);
    }

}
