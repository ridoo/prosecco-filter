
package org.n52.prosecco.web.sos;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.RequestFilterEngine;
import org.n52.prosecco.web.FilterException;
import org.n52.prosecco.web.request.FilterContext;

public final class SosFilterGetRequestService {

    private final RequestFilterEngine filterEngine;

    public SosFilterGetRequestService(RequestFilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public String filter(HttpServletRequest request, FilterContext context) throws FilterException {
        String operation = request.getParameter("request");
        SosQueryFormatter queryFormatter = new SosQueryFormatter();
        return !"GetCapabilities".equalsIgnoreCase(operation)
                ? queryFormatter.format(filterEngine.evaluate(context))
                : queryFormatter.format(context);
    }


}
