
package org.n52.prosecco.web;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
     */
    String filterGET(HttpServletRequest request) throws FilterException;

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
            return authorities.stream()
                              .map(a -> a.getAuthority())
                              .collect(Collectors.toSet());
        }
    }

}
