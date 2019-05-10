
package org.n52.prosecco.web.sos;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.DroppedQueryConditionException;
import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.GetRequestFilter;
import org.n52.prosecco.web.request.FilterContext;

public final class SosRequestGetFilter implements GetRequestFilter {

    private final RequestFilterEngine filterEngine;

    public SosRequestGetFilter(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }
    
    @Override
    public String filter(HttpServletRequest request, FilterContext context)
            throws FilterException, DroppedQueryConditionException {
        String operation = request.getParameter("request");
        SosQueryFormatter queryFormatter = new SosQueryFormatter();
        return !"GetCapabilities".equalsIgnoreCase(operation)
                ? queryFormatter.format(filterEngine.evaluate(context))
                : queryFormatter.format(context);
    }


}
