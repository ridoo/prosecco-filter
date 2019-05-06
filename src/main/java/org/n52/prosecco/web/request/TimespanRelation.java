package org.n52.prosecco.web.request;

import java.util.Arrays;
import java.util.List;

public enum TimespanRelation {
    BETWEEN("between"), 
    BEFORE("before"), 
    AFTER("after"), 
    ON("");
    
    private final String relation;

    private TimespanRelation(String relation) {
        this.relation = relation;
    }

    public String getRelation() {
        return relation;
    }
    
    public static TimespanRelation findValue(String value) {
        List<TimespanRelation> values = Arrays.asList(values());
        return values.stream()
                     .filter(v -> v.relation.equalsIgnoreCase(value))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}