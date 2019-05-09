package org.n52.prosecco;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface AuthenticationContext {

    /**
     * @return all roles of the current authenticaion context
     */
    Set<String> getRoles();
    
    public static final class AuthenticationContextBuilder {

        private static final AuthenticationContext EMPTY_AUTH_CONTEXT = new AuthenticationContext() {
            @Override
            public Set<String> getRoles() {
                return Collections.emptySet();
            }
        };
        
        public static AuthenticationContext empty() {
            return EMPTY_AUTH_CONTEXT;
        }
        
        public static AuthenticationContext withRoles(Set<String> roles) {
            return new AuthenticationContext() {
                @Override
                public Set<String> getRoles() {
                    return new HashSet<>(roles);
                }
            };
        }
    }
    
}
