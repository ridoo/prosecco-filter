
package org.n52.prosecco.web.dataset;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.DroppedQueryConditionException;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.GetRequestFilter;
import org.n52.prosecco.web.request.FilterContext;

public class DatasetRequestGetFilter implements GetRequestFilter {

    private final RequestFilterEngine filterEngine;

    public DatasetRequestGetFilter(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public String filter(HttpServletRequest request, FilterContext context) throws DroppedQueryConditionException {
        FilterContext filteredContext = filterEngine.evaluate(context);
        DatasetQueryFormatter queryFormatter = new DatasetQueryFormatter();
        return queryFormatter.format(filteredContext);
    }

}
