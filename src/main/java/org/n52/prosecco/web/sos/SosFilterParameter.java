package org.n52.prosecco.web.sos;

import java.util.Arrays;
import java.util.List;

enum SosFilterParameter {
    
    FEATURE("featureOfInterest"), 
    PHENOMENON("observedProperty"), 
    PROCEDURE("procedure"), 
    OFFERING("offering"), 
    TIMESPAN("temporalFilter");
    
    final String filterName;

    private SosFilterParameter(String filterName) {
        this.filterName = filterName;
    }

    static boolean isKnown(String key) {
        List<SosFilterParameter> values = Arrays.asList(values());
        return values.stream().anyMatch(value -> value.filterName.equalsIgnoreCase(key));
    }
}