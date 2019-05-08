package org.n52.prosecco;

import java.util.Set;

public interface AuthenticationContext {

    /**
     * @return all roles of the current authenticaion context
     */
    Set<String> getRoles();
}
