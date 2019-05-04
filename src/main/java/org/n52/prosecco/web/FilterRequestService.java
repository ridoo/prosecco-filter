
package org.n52.prosecco.web;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.n52.prosecco.engine.filter.FilterContext;
import org.n52.prosecco.web.sos.FilterRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public interface FilterRequestService {

    /**
     * Filters the contetnt of a given request according configured rules. The content may be KVP query
     * parameters from a GET request or a simple POST payload. The filtered result may have to be encoded for
     * further use (if needed).
     * 
     * @param request
     *        the request to filter
     * @return the filtered request content
     * @throws FilterRequestException
     *         when request is invalid
     */
    String filter(HttpServletRequest request) throws FilterRequestException;

    /**
     * Creates a {@link FilterContext} from the given request and roles.
     * 
     * @param roles
     *        the roles
     * @param request
     *        the actual request
     * @return a filter context containing all relevant values
     * @throws FilterRequestException
     *         when request is invalid
     */
    FilterContext createFilterContext(Set<String> roles, HttpServletRequest request) throws FilterRequestException;

    /**
     * @return all roles of the current authenticaion context
     */
    default Set<String> getRoles() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return Collections.emptySet();
        } else {
            Collection< ? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        }
    }

}
