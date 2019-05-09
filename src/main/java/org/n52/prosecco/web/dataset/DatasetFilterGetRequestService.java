package org.n52.prosecco.web.dataset;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.request.FilterContext;

public class DatasetFilterGetRequestService {

    private final RequestFilterEngine filterEngine;

    public DatasetFilterGetRequestService(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public String filter(HttpServletRequest request, FilterContext context) {
        FilterContext filteredContext = filterEngine.evaluate(context);
        DatasetQueryFormatter queryFormatter = new DatasetQueryFormatter();
        return queryFormatter.format(filteredContext);
    }
    
}
