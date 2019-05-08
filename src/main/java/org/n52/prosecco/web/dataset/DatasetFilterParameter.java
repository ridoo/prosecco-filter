package org.n52.prosecco.web.dataset;

import java.util.Arrays;
import java.util.List;

enum DatasetFilterParameter {
    
    FEATURE("features"), 
    PHENOMENON("phenomena"), 
    PROCEDURE("procedures"), 
    OFFERING("offerings"),
    CATEGORY("category"),
    TIMESPAN("timespan");
    
    final String filterName;

    private DatasetFilterParameter(String filterName) {
        this.filterName = filterName;
    }

    static boolean isKnown(String key) {
        List<DatasetFilterParameter> values = Arrays.asList(values());
        return values.stream().anyMatch(value -> value.filterName.equalsIgnoreCase(key));
    }
}