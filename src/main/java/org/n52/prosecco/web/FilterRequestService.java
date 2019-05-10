
package org.n52.prosecco.web;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.filter.DroppedQueryConditionException;

public interface FilterRequestService {

    /**
     * Filters the content of an HTTP GET request according configured rules. The filtered result may have to
     * be encoded for further use (if needed).
     * 
     * @param request
     *        the request to filter
     * @return the filtered request content
     * @throws FilterException
     *         when request is invalid
     * @throws DroppedQueryConditionException
     *         in cases where filtering would falsify the actual query (e.g. dropping an AND condition)
     */
    String filterGET(HttpServletRequest request) throws FilterException, DroppedQueryConditionException;

    /**
     * Filters the content of an HTTP POST request according configured rules.
     * 
     * @param request
     *        the request to filter
     * @return the filtered request content
     * @throws FilterException
     *         when request is invalid
     */
    String filterPOST(HttpServletRequest request) throws FilterException;

}
