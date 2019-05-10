
package org.n52.prosecco.web;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.DroppedQueryConditionException;
import org.n52.prosecco.web.request.FilterContext;

public interface GetRequestFilter {

    String filter(HttpServletRequest request, FilterContext context)
            throws FilterException, DroppedQueryConditionException;

}
