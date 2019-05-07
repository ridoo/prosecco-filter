package org.n52.prosecco.web;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public interface AuthenticationContext {

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
