package org.n52.prosecco.filter;

import java.util.Collections;
import java.util.Set;

public class DroppedQueryConditionException extends Exception {

    private static final long serialVersionUID = 8020120302988018953L;
    
    private final String parameter;
    
    private final Set<String> values;

    public DroppedQueryConditionException(String parameter, Set<String> values, String message) {
        super(message);
        this.parameter = parameter;
        this.values = values;
    }

    public Set<String> getValues() {
        return Collections.unmodifiableSet(values);
    }

    public String getParameter() {
        return parameter;
    }
}
